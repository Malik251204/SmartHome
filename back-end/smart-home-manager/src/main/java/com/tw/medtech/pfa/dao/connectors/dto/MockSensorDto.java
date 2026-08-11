package com.tw.medtech.pfa.dao.connectors.dto;

public record MockSensorDto(
        Long id,
        String name,
        String type,
        String unit,
        String status,
        String data
) {}