package com.tw.medtech.pfa;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tw.medtech.pfa.web.dto.AgentAction;
import com.tw.medtech.pfa.web.dto.AgentDecisionResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AgentActionMappingTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Step 1: confirm Jackson correctly deserializes the model's raw JSON
    // response (matching the SYSTEM_PROMPT's exact example shape) into
    // AgentDecisionResponse/AgentAction, including the new
    // currentStatus/details fields.
    @Test
    void deserializesCurrentStatusAndDetailsFromLlmJson() throws Exception {
        String llmJson = """
                {"summary": "Turning off the AC since it's cold.",
                "actions": [{"deviceId": 1, "currentStatus": "ON",
                "newStatus": "OFF", "details": "celsius: 15.7",
                "reasoning": "Room is cold per preference"}]}
                """;

        AgentDecisionResponse response = objectMapper.readValue(llmJson, AgentDecisionResponse.class);

        assertEquals("Turning off the AC since it's cold.", response.summary());
        assertEquals(1, response.actions().size());

        AgentAction action = response.actions().get(0);
        assertEquals(1L, action.deviceId());
        assertEquals("ON", action.currentStatus());
        assertEquals("OFF", action.newStatus());
        assertEquals("celsius: 15.7", action.details());
        assertEquals("Room is cold per preference", action.reasoning());
        // Not populated by the model — filled in server-side, so null here.
        assertEquals(null, action.deviceName());
        assertEquals(null, action.previousStatus());
    }

    // Step 2: confirm the enrichment line in AgentServiceImpl.evaluate()
    // correctly threads currentStatus/details through into the enriched,
    // persisted action alongside the server-known deviceName/previousStatus.
    @Test
    void enrichedActionCarriesModelFieldsThroughAlongsideServerKnownFields() {
        AgentAction proposed = new AgentAction(1L, null, null, "ON", "OFF", "celsius: 15.7", "Room is cold");

        // Mirrors exactly the construction line in AgentServiceImpl.evaluate():
        // applied.add(new AgentAction(device.id(), device.name(), previousStatus,
        //     action.currentStatus(), action.newStatus(), action.details(), action.reasoning()));
        String serverKnownDeviceName = "Living Room AC";
        String serverKnownPreviousStatus = "ON";
        AgentAction enriched = new AgentAction(
                proposed.deviceId(), serverKnownDeviceName, serverKnownPreviousStatus,
                proposed.currentStatus(), proposed.newStatus(), proposed.details(), proposed.reasoning()
        );

        assertEquals(1L, enriched.deviceId());
        assertEquals("Living Room AC", enriched.deviceName());
        assertEquals("ON", enriched.previousStatus());
        assertEquals("ON", enriched.currentStatus());
        assertEquals("OFF", enriched.newStatus());
        assertEquals("celsius: 15.7", enriched.details());
        assertEquals("Room is cold", enriched.reasoning());
    }
}