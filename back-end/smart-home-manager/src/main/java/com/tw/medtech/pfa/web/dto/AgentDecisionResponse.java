package com.tw.medtech.pfa.web.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

// Deserialized directly from the LLM's raw JSON output — see
// AgentServiceImpl.SYSTEM_PROMPT for the exact shape it's instructed to
// produce. Small local models occasionally omit a field or emit an extra
// one; ignoreUnknown covers the latter, defensive null-handling in
// AgentServiceImpl covers the former.
@JsonIgnoreProperties(ignoreUnknown = true)
public record AgentDecisionResponse(
        String summary,
        List<AgentAction> actions
) {}
