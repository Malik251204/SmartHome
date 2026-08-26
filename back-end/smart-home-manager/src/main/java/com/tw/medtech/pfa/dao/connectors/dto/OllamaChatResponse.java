package com.tw.medtech.pfa.dao.connectors.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// Ollama's /api/chat response includes several other fields (created_at,
// done_reason, total_duration, eval_count, ...) — ignored, we only need
// the assistant's message content.
@JsonIgnoreProperties(ignoreUnknown = true)
public record OllamaChatResponse(
        OllamaChatRequest.OllamaMessage message,
        boolean done
) {}
