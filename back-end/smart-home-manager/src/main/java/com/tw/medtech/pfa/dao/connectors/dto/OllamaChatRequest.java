package com.tw.medtech.pfa.dao.connectors.dto;

import java.util.List;

// Ollama's /api/chat request shape. "format": "json" constrains the
// model's output to valid JSON syntax (not our specific schema — the
// system prompt is what tells it the actual shape we need). Temperature
// is set low (see application.yaml) on purpose — this is a "read
// structured data, output one coherent decision" task, not a creative
// one, and default sampling temperature was producing responses where
// the summary and the actual proposed action disagreed with each other.
public record OllamaChatRequest(
        String model,
        List<OllamaMessage> messages,
        String format,
        boolean stream,
        OllamaOptions options
) {
    public record OllamaMessage(String role, String content) {}
    public record OllamaOptions(Double temperature) {}
}
