package com.tw.medtech.pfa.dao.connectors.config;

import com.tw.medtech.pfa.dao.connectors.OllamaClient;
import com.tw.medtech.pfa.dao.connectors.SensorClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

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

    @Bean
    public RestClient ollamaRestClient(@Value("${ollama.base-url}") String baseUrl) {
        // Local LLM inference (especially on a 4GB-VRAM card) can take
        // well over the default timeout — read timeout set generously
        // rather than tuned tight, since a slow answer is fine but a
        // falsely-timed-out one just gets treated as a failed evaluation.
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(120));

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
    }

    @Bean
    public OllamaClient ollamaClient(RestClient ollamaRestClient,
                                      @Value("${ollama.model}") String model,
                                      @Value("${ollama.temperature}") double temperature) {
        return new OllamaClient(ollamaRestClient, model, temperature);
    }
}