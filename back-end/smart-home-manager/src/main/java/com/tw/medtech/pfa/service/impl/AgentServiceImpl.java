package com.tw.medtech.pfa.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tw.medtech.pfa.dao.connectors.dto.SensorResponse;
import com.tw.medtech.pfa.dao.repository.AgentDecisionRepository;
import com.tw.medtech.pfa.dao.repository.PreferenceRepository;
import com.tw.medtech.pfa.dao.repository.RoomRepository;
import com.tw.medtech.pfa.model.AgentDecision;
import com.tw.medtech.pfa.model.Preference;
import com.tw.medtech.pfa.model.Room;
import com.tw.medtech.pfa.model.enums.DeviceStatus;
import com.tw.medtech.pfa.service.AgentService;
import com.tw.medtech.pfa.service.DeviceService;
import com.tw.medtech.pfa.web.dto.AgentAction;
import com.tw.medtech.pfa.web.dto.AgentDecisionDto;
import com.tw.medtech.pfa.web.dto.AgentDecisionResponse;
import com.tw.medtech.pfa.web.dto.AgentRunSummary;
import com.tw.medtech.pfa.web.dto.DeviceDto;
import com.tw.medtech.pfa.web.dto.RoomDto;
import com.tw.medtech.pfa.web.dto.SensorDetail;
import com.tw.medtech.pfa.web.dto.mapper.RoomMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AgentServiceImpl implements AgentService {

    // Mirrors the frontend's DEVICE_STATUS_PAIR (types/device.ts) — kept
    // as a separate copy here rather than a shared source of truth,
    // same as the rest of this backend/frontend split. Used both to tell
    // the LLM what's valid per device type, and to reject anything it
    // proposes outside that pair.
    private static final Map<String, List<String>> VALID_STATUSES_BY_TYPE = Map.of(
            "AC", List.of("OFF", "ON"),
            "LIGHT_BULB", List.of("OFF", "ON"),
            "CURTAINS", List.of("CLOSED", "OPEN")
    );

    private static final String SYSTEM_PROMPT = """
            You are a smart home automation assistant. You will be given one \
            user's stated preference for a room, that room's current devices \
            and their status, current ambient sensor readings, and the \
            current day and time.

            Follow these steps before answering:
            1. Read the Devices list below and note each device's exact \
            current status word-for-word (e.g. "ON", "OFF", "OPEN", \
            "CLOSED"). Never guess or assume a status — use only what is \
            written next to "currently".
            2. Read the preference and decide which device(s), if any, it \
            is actually about. If none of the room's devices relate to \
            what the preference is asking for, say so plainly in your \
            summary and return an empty actions list. Do not mention or \
            reason about a device the preference isn't about.
            3. Using only the exact status from step 1 and the current \
            sensor readings, decide whether a change is warranted. Most \
            of the time no change is needed — an empty actions list is \
            often the correct answer.
            4. Never propose changing a device to the status it is \
            already at (you already know this status from step 1).
            5. Never propose a status a device's type doesn't support \
            (each device lists its valid statuses).
            6. Before answering, check that your "summary" and your \
            "actions" agree with each other — they must never contradict \
            each other or describe a different outcome.

            Example: if a device is listed as "currently ON" and the \
            preference calls for it to be off given current conditions, \
            propose turning it OFF. Do not say a device is "already off" \
            or "already on" unless that is literally what the Devices \
            list says for that device.

            Respond with ONLY valid JSON, no other text, in exactly this \
            shape:
            {"summary": "one short sentence explaining your decision", \
            "actions": [{"deviceId": 12, "newStatus": "ON", "reasoning": \
            "short reason for this one action"}]}
            """;

    private final PreferenceRepository preferenceRepository;
    private final RoomRepository roomRepository;
    private final RoomMapper roomMapper;
    private final DeviceService deviceService;
    // Replaces the hand-rolled OllamaClient — auto-configured by
    // spring-ai-starter-model-ollama against spring.ai.ollama.* (see
    // application.yaml) and turned into a bean in AiConfig. The
    // .entity(AgentDecisionResponse.class) call below handles both
    // asking for and parsing structured JSON output, replacing what
    // used to be a manual objectMapper.readValue() on a raw string.
    private final ChatClient chatClient;
    private final AgentDecisionRepository agentDecisionRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${agent.cooldown-minutes:30}")
    private int cooldownMinutes;

    @Override
    @Transactional
    // Needed for the scheduled call path specifically: AgentScheduler
    // runs on a background thread with no HTTP request and therefore no
    // Open-Session-In-View, and RoomMapper touches Room's lazy
    // collections (devices/users/sensorIds). Safe with respect to the
    // per-(preference, room) failure isolation below — every exception
    // is caught inside the loop, so nothing ever propagates out to
    // trigger a rollback of earlier iterations' already-applied changes.
    public AgentRunSummary runOnce() {
        List<Preference> enabledPreferences = preferenceRepository.findByEnabledTrue();
        List<AgentDecisionDto> decisions = new ArrayList<>();

        for (Preference preference : enabledPreferences) {
            List<Room> rooms = preference.getRoom() != null
                    ? List.of(preference.getRoom())
                    : roomRepository.findByUsersContaining(preference.getUser());

            for (Room room : rooms) {
                // Isolated per (preference, room) pair on purpose — one
                // bad LLM response or a transient Ollama/network error
                // shouldn't abort evaluation of every other pair in this
                // run.
                try {
                    decisions.add(toDto(evaluate(preference, room)));
                } catch (Exception e) {
                    log.error("Agent evaluation failed for preference {} / room {}",
                            preference.getId(), room.getId(), e);
                    decisions.add(toDto(persist(preference, room, null, null,
                            "Evaluation failed: " + e.getMessage(), List.of(), false)));
                }
            }
        }

        long applied = decisions.stream().filter(AgentDecisionDto::hasActions).count();
        return new AgentRunSummary(decisions.size(), (int) applied, decisions);
    }

    @Override
    public List<AgentDecisionDto> getRecentDecisions(int limit) {
        return agentDecisionRepository.findTop50ByOrderByCreatedAtDesc().stream()
                .limit(limit)
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private AgentDecision evaluate(Preference preference, Room room) throws Exception {
        if (isInCooldown(preference.getId(), room.getId())) {
            return persist(preference, room, null, null,
                    "Skipped — a change was already applied for this preference/room within the last "
                            + cooldownMinutes + " minutes.",
                    List.of(), false);
        }

        RoomDto roomDto = roomMapper.mapToDto(room);
        String sensorSnapshot = objectMapper.writeValueAsString(roomDto.sensors());
        List<SensorDetail> sensorDetails = buildSensorDetails(roomDto);
        String sensorDetailsJson = objectMapper.writeValueAsString(sensorDetails);
        String userPrompt = buildUserPrompt(preference, roomDto);

        AgentDecisionResponse decision;
        try {
            decision = chatClient.prompt()
                    .system(SYSTEM_PROMPT)
                    .user(userPrompt)
                    .call()
                    .entity(AgentDecisionResponse.class);
            if (decision == null) {
                throw new IllegalStateException("Model returned no parseable response");
            }
        } catch (Exception e) {
            log.warn("Could not get/parse a decision from the model for preference {} / room {}: {}",
                    preference.getId(), room.getId(), e.getMessage());
            return persist(preference, room, sensorSnapshot, sensorDetailsJson,
                    "LLM call failed or returned unparseable output: " + e.getMessage(), List.of(), false);
        }

        List<AgentAction> proposed = decision.actions() != null ? decision.actions() : List.of();
        List<AgentAction> applied = new ArrayList<>();

        for (AgentAction action : proposed) {
            DeviceDto device = findDevice(roomDto, action.deviceId());
            if (device == null) {
                log.warn("Agent proposed an action for device {} which isn't in room {} — dropped.",
                        action.deviceId(), room.getId());
                continue;
            }
            if (!isValidStatus(device.type(), action.newStatus())) {
                log.warn("Agent proposed status '{}' for device {} (type {}) — not a valid status, dropped.",
                        action.newStatus(), device.id(), device.type());
                continue;
            }
            if (action.newStatus().equals(device.status())) {
                continue; // no-op, not a real action
            }

            String previousStatus = device.status();
            deviceService.updateStatus(device.id(), DeviceStatus.valueOf(action.newStatus()));
            // Enriched with what we know server-side (device name, its
            // real prior status) — never trust the model to report these
            // itself, it was never asked to and has no reliable way to.
            applied.add(new AgentAction(device.id(), device.name(), previousStatus, action.newStatus(), action.reasoning()));
        }

        String summary = decision.summary() != null ? decision.summary() : "(no summary provided)";
        return persist(preference, room, sensorSnapshot, sensorDetailsJson, summary, applied, !applied.isEmpty());
    }

    private boolean isInCooldown(Long preferenceId, Long roomId) {
        return agentDecisionRepository
                .findFirstByPreferenceIdAndRoomIdAndHasActionsTrueOrderByCreatedAtDesc(preferenceId, roomId)
                .map(last -> last.getCreatedAt().isAfter(Instant.now().minus(cooldownMinutes, ChronoUnit.MINUTES)))
                .orElse(false);
    }

    private String buildUserPrompt(Preference preference, RoomDto room) {
        StringBuilder sb = new StringBuilder();
        sb.append("Current time: ")
                .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEEE, HH:mm", Locale.ENGLISH)))
                .append("\n\n");
        sb.append("Room: ").append(room.name()).append("\n\n");
        sb.append("Preference (from the user who owns this room): \"").append(preference.getText()).append("\"\n\n");

        sb.append("Devices:\n");
        if (room.devices().isEmpty()) {
            sb.append("(none)\n");
        }
        for (DeviceDto device : room.devices()) {
            List<String> validStatuses = VALID_STATUSES_BY_TYPE.getOrDefault(device.type(), List.of());
            sb.append("- [deviceId=").append(device.id()).append("] ")
                    .append(device.name()).append(" (").append(device.type()).append("): currently ")
                    .append(device.status()).append(". Valid statuses: ")
                    .append(String.join(", ", validStatuses)).append("\n");
        }

        sb.append("\nSensor readings:\n");
        if (room.sensors().isEmpty()) {
            sb.append("(none)\n");
        }
        for (SensorResponse sensor : room.sensors()) {
            sb.append("- ").append(describeSensor(sensor)).append("\n");
        }

        return sb.toString();
    }

    // Single source of truth for turning a raw sensor reading into
    // something readable — used both to build the LLM's prompt text and
    // to build the structured, persisted SensorDetail list.
    private SensorDetail parseSensorDetail(SensorResponse sensor) {
        try {
            Map<String, Object> data = objectMapper.readValue(sensor.data(), Map.class);
            return switch (sensor.type()) {
                case "LUX" -> new SensorDetail("LUX", "Light level", data.get("lux") + " lux");
                case "TEMPERATURE" -> new SensorDetail("TEMPERATURE", "Temperature", data.get("celsius") + " \u00b0C");
                case "OCCUPANCY" -> new SensorDetail("OCCUPANCY", "Occupancy", data.get("count") + " people");
                default -> new SensorDetail(sensor.type(), sensor.type(), sensor.data());
            };
        } catch (Exception e) {
            return new SensorDetail(sensor.type(), sensor.type(), "(unreadable reading)");
        }
    }

    private String describeSensor(SensorResponse sensor) {
        SensorDetail detail = parseSensorDetail(sensor);
        return detail.label() + ": " + detail.value();
    }

    private List<SensorDetail> buildSensorDetails(RoomDto room) {
        return room.sensors().stream().map(this::parseSensorDetail).collect(Collectors.toList());
    }

    private DeviceDto findDevice(RoomDto room, Long deviceId) {
        return room.devices().stream()
                .filter(d -> d.id().equals(deviceId))
                .findFirst()
                .orElse(null);
    }

    private boolean isValidStatus(String deviceType, String status) {
        return VALID_STATUSES_BY_TYPE.getOrDefault(deviceType, List.of()).contains(status);
    }

    private AgentDecision persist(Preference preference, Room room, String sensorSnapshot, String sensorDetailsJson,
                                   String summary, List<AgentAction> applied, boolean hasActions) {
        String actionsJson;
        try {
            actionsJson = objectMapper.writeValueAsString(applied);
        } catch (Exception e) {
            actionsJson = "[]";
        }

        AgentDecision decision = AgentDecision.builder()
                .preferenceId(preference.getId())
                .preferenceText(preference.getText())
                .userId(preference.getUser().getId())
                .roomId(room.getId())
                .roomName(room.getName())
                .sensorSnapshot(sensorSnapshot)
                .sensorDetailsJson(sensorDetailsJson)
                .summary(summary)
                .actionsJson(actionsJson)
                .hasActions(hasActions)
                .build();

        return agentDecisionRepository.save(decision);
    }

    private AgentDecisionDto toDto(AgentDecision d) {
        return new AgentDecisionDto(
                d.getId(), d.getPreferenceId(), d.getPreferenceText(), d.getUserId(),
                d.getRoomId(), d.getRoomName(), d.getSensorSnapshot(), d.getSensorDetailsJson(),
                d.getSummary(), d.getActionsJson(), d.isHasActions(), d.getCreatedAt()
        );
    }
}
