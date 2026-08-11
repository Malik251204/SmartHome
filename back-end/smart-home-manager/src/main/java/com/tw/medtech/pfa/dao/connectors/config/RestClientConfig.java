package com.tw.medtech.pfa.dao.connectors.config;

import com.tw.medtech.pfa.dao.connectors.SensorClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient sensorRestClient() {
        return RestClient.builder()
                .baseUrl("http://localhost:8081")
                .build();
    }

    @Bean
    public SensorClient sensorClient(RestClient sensorRestClient) {
        return new SensorClient(sensorRestClient);
    }
}