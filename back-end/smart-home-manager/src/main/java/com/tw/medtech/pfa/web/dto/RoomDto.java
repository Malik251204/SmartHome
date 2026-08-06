package com.tw.medtech.pfa.web.dto;

import com.tw.medtech.pfa.dao.connectors.dto.SensorResponse;

import java.util.List;

public record RoomDto(
        Long id,
        String name,
        List<UserDto> users,
        List<DeviceDto> devices,
        List<SensorResponse> sensors
) {}