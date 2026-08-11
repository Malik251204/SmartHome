package com.teamwill.pfa.medtech.home_manager.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamwill.pfa.medtech.home_manager.entity.Reading;
import com.teamwill.pfa.medtech.home_manager.entity.Sensor;
import com.teamwill.pfa.medtech.home_manager.repository.ReadingRepository;
import com.teamwill.pfa.medtech.home_manager.repository.SensorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

// Simulates sensors "updating live" in software, replacing the real
// backend's ESP32 poll loop. No hardware, no Wokwi — just a periodic nudge
// to each sensor's data plus an appended Reading snapshot, which is what
// feeds the frontend's sparkline history.
@Component
@RequiredArgsConstructor
public class SensorSimulationScheduler {

    private static final int MAX_READINGS_PER_SENSOR = 30;

    private final SensorRepository sensorRepository;
    private final ReadingRepository readingRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Scheduled(fixedRate = 1000)
    public void tick() {
        List<Sensor> sensors = sensorRepository.findAll();
        for (Sensor sensor : sensors) {
            if (!"on".equalsIgnoreCase(sensor.getStatus())) {
                continue; // off sensors don't drift
            }
            try {
                String updatedData = drift(sensor);
                sensor.setData(updatedData);
                sensorRepository.save(sensor);
                recordReading(sensor.getId(), updatedData);
            } catch (Exception e) {
                // Simulation is decorative — never let a bad data blob take
                // down the scheduled tick for every other sensor.
            }
        }
    }

    @SuppressWarnings("unchecked")
    private String drift(Sensor sensor) throws Exception {
        Map<String, Object> data = sensor.getData() == null || sensor.getData().isBlank()
                ? new LinkedHashMap<>()
                : objectMapper.readValue(sensor.getData(), LinkedHashMap.class);

        ThreadLocalRandom random = ThreadLocalRandom.current();

        switch (sensor.getType()) {
            case LUX -> {
                double lux = toDouble(data.get("lux"), 400);
                lux = clamp(lux + random.nextInt(-15, 16), 0, 1000);
                data.put("lux", (int) lux);
            }
            case TEMPERATURE -> {
                double celsius = toDouble(data.get("celsius"), 21);
                celsius = clamp(celsius + (random.nextDouble() - 0.5) * 0.6, 15, 30);
                data.put("celsius", Math.round(celsius * 10.0) / 10.0);
            }
            case OCCUPANCY -> {
                double count = toDouble(data.get("count"), 0);
                count = clamp(count + random.nextInt(-2, 3), 0, 10);
                data.put("count", (int) count);
            }
        }

        return objectMapper.writeValueAsString(data);
    }

    private void recordReading(Long sensorId, String data) {
        readingRepository.save(Reading.builder()
                .sensorId(sensorId)
                .recordedAt(Instant.now())
                .data(data)
                .build());

        List<Reading> all = readingRepository.findBySensorIdOrderByRecordedAtAsc(sensorId);
        if (all.size() > MAX_READINGS_PER_SENSOR) {
            List<Long> excessIds = all.stream()
                    .limit((long) all.size() - MAX_READINGS_PER_SENSOR)
                    .map(Reading::getId)
                    .collect(Collectors.toList());
            readingRepository.deleteBySensorIdAndIdIn(sensorId, excessIds);
        }
    }

    private double toDouble(Object value, double fallback) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        return fallback;
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
