package com.teamwill.pfa.medtech.home_manager.mapper;

import com.teamwill.pfa.medtech.home_manager.dto.SensorDto;
import com.teamwill.pfa.medtech.home_manager.entity.Sensor;

public class SensorMapper {

    public static SensorDto mapToDto(Sensor sensor) {
        return SensorDto.builder()
                .id(sensor.getId())
                .name(sensor.getName())
                .type(sensor.getType())
                .unit(sensor.getUnit())
                .status(sensor.getStatus())
                .data(sensor.getData())
                .build();
    }

    public static Sensor mapToEntity(SensorDto dto) {
        return Sensor.builder()
                .id(dto.getId())
                .name(dto.getName())
                .type(dto.getType())
                .unit(dto.getUnit())
                .status(dto.getStatus())
                .data(dto.getData())
                .build();
    }
}
