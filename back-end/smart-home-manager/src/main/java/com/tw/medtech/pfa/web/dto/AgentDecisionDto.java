package com.tw.medtech.pfa.web.dto;

import java.time.Instant;

public record AgentDecisionDto(
        Long id,
        Long preferenceId,
        String preferenceText,
        Long userId,
        Long roomId,
        String roomName,
        String sensorSnapshot,
        String summary,
        String actionsJson,
        boolean hasActions,
        Instant createdAt
) {}
