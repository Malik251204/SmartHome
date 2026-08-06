package com.tw.medtech.pfa.dao.connectors.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient sensorRestClient() {
        // backend/mock (the sensor simulation service) runs on 8081.
        // This was pointing at 8080 — this app's own port — which can't be
        // right for a client meant to reach a separate service. Not wired
        // into SensorServiceImpl yet; that integration is still open,
        // deliberately left untouched this session.
        return RestClient.builder()
                .baseUrl("http://localhost:8081")
                .build();
    }
}
