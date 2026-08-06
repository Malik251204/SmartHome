package com.tw.medtech.pfa.web.dto;

public record DeviceDto(
        Long id,
        String name,
        String unit,
        String status,
        Long roomId,
        String roomName
) {}