package com.tw.medtech.pfa.dao.connectors;

import com.tw.medtech.pfa.dao.connectors.dto.SensorResponse;
import org.springframework.web.client.RestClient;

public class SensorClient {

    private final RestClient restClient;

    public SensorClient(RestClient sensorRestClient) {
        this.restClient = sensorRestClient;
    }

    public SensorResponse getSensorById(Long id) {
        return restClient.get()
                .uri("/api/sensors/{id}", id)
                .retrieve()
                .body(SensorResponse.class);
    }

}
