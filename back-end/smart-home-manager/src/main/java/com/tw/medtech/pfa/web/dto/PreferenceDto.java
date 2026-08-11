package com.tw.medtech.pfa.web.dto;

import java.time.Instant;

public record PreferenceDto(
        Long id,
        Long userId,
        Long roomId,
        String roomName,
        String text,
        boolean enabled,
        Instant createdAt
) {}
