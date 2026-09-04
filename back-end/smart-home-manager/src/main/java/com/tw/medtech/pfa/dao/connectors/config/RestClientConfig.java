package com.tw.medtech.pfa.dao.connectors.config;

import com.tw.medtech.pfa.dao.connectors.SensorClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    // Was a hardcoded string until the docker-compose work — meant fine
    // on one machine where "localhost" always means the same thing, but
    // breaks the moment this and smart-home-mock become separate
    // containers.
    @Bean
    public RestClient sensorRestClient(@Value("${sensor.base-url:http://localhost:8081}") String baseUrl) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @Bean
    public SensorClient sensorClient(RestClient sensorRestClient) {
        return new SensorClient(sensorRestClient);
    }

    // No Ollama beans here anymore — spring-ai-starter-model-ollama
    // auto-configures its own OllamaChatModel and ChatClient.Builder
    // directly from the spring.ai.ollama.* properties in
    // application.yaml. See AiConfig for the one small thing we still
    // wire ourselves (turning that builder into a ChatClient bean).
}
