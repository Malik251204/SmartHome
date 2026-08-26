package com.tw.medtech.pfa.web.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AgentAction(
        Long deviceId,
        String newStatus,
        String reasoning
) {}
