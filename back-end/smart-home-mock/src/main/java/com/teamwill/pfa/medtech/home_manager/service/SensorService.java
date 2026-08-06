package com.teamwill.pfa.medtech.home_manager.service;

import com.teamwill.pfa.medtech.home_manager.dto.ReadingDto;
import com.teamwill.pfa.medtech.home_manager.dto.SensorDto;

import java.util.List;

public interface SensorService {
    SensorDto createSensor(SensorDto dto);

    List<SensorDto> getAllSensors();

    SensorDto getSensorById(Long id);

    // Direct, instant, synchronous update — no command queue. This is the
    // mock backend's only write path for a sensor's operational state.
    SensorDto updateSensor(Long id, SensorDto dto);

    void deleteSensor(Long id);

    List<ReadingDto> getReadings(Long id, int limit);
}
