package com.tw.medtech.pfa.dao.connectors.dto;

public record SensorResponse(
        Long id,
        String name,
        String type,
        String unit,
        String status,
        String data,      // raw JSON string, e.g. {"isOpen":false,"roomLightLux":363}
        String roomId,
        String roomName
) {}
