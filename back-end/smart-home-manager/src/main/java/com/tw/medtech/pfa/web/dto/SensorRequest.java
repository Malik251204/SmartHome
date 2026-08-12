package com.tw.medtech.pfa.web.dto;

public record SensorRequest(
        String name,
        String type,
        String unit,
        String status,
        String data,
        Long roomId
) {}