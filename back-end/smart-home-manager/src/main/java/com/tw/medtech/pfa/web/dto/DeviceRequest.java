package com.tw.medtech.pfa.web.dto;

public record DeviceRequest(
        String name,
        String type,
        Double unit,
        String status,
        Long roomId
) {}