package com.tw.medtech.pfa.dao.connectors;

import com.tw.medtech.pfa.dao.connectors.dto.MockSensorDto;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

public class SensorClient {

    private final RestClient restClient;

    public SensorClient(RestClient sensorRestClient) {
        this.restClient = sensorRestClient;
    }

    public MockSensorDto getSensorById(Long id) {
        try {
            return restClient.get()
                    .uri("/api/sensors/{id}", id)
                    .retrieve()
                    .body(MockSensorDto.class);
        } catch (RestClientException e) {
            // Sensor doesn't exist in the mock backend (deleted, or its
            // in-memory DB was wiped on restart) — treat as "not found"
            // rather than letting the error bubble up and break the room.
            return null;
        }
    }

    public List<MockSensorDto> getAllSensors() {
        try {
            return restClient.get()
                    .uri("/api/sensors")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<MockSensorDto>>() {});
        } catch (RestClientException e) {
            return List.of();
        }
    }

    public MockSensorDto createSensor(MockSensorDto sensor) {
        return restClient.post()
                .uri("/api/sensors")
                .contentType(MediaType.APPLICATION_JSON)
                .body(sensor)
                .retrieve()
                .body(MockSensorDto.class);
    }
}