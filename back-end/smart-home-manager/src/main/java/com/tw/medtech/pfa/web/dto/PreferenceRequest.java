package com.tw.medtech.pfa.web.dto;

public record PreferenceRequest(Long userId, Long roomId, String text, boolean enabled) {}
