package com.tw.medtech.pfa.dao.connectors;

import com.tw.medtech.pfa.dao.connectors.dto.OllamaChatRequest;
import com.tw.medtech.pfa.dao.connectors.dto.OllamaChatResponse;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.util.List;

public class OllamaClient {

    private final RestClient restClient;
    private final String model;
    private final double temperature;

    public OllamaClient(RestClient ollamaRestClient, String model, double temperature) {
        this.restClient = ollamaRestClient;
        this.model = model;
        this.temperature = temperature;
    }

    // Returns the assistant's raw JSON string content — parsing it into
    // our AgentDecisionResponse shape is AgentServiceImpl's job, not
    // this client's, same division of responsibility as SensorClient
    // returning MockSensorDto rather than domain objects.
    public String chat(String systemPrompt, String userPrompt) {
        OllamaChatRequest request = new OllamaChatRequest(
                model,
                List.of(
                        new OllamaChatRequest.OllamaMessage("system", systemPrompt),
                        new OllamaChatRequest.OllamaMessage("user", userPrompt)
                ),
                "json",
                false,
                new OllamaChatRequest.OllamaOptions(temperature)
        );

        OllamaChatResponse response = restClient.post()
                .uri("/api/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(OllamaChatResponse.class);

        if (response == null || response.message() == null || response.message().content() == null) {
            throw new IllegalStateException("Ollama returned an empty response");
        }

        return response.message().content();
    }
}
