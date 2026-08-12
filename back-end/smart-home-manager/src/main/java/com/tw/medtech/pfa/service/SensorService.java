package com.tw.medtech.pfa.service;

import com.tw.medtech.pfa.dao.connectors.dto.SensorResponse;
import com.tw.medtech.pfa.web.dto.SensorRequest;

import java.util.List;

public interface SensorService {
    List<SensorResponse> getAllSensors();
    SensorResponse getSensorById(Long id);
    SensorResponse createSensor(SensorRequest request);
    SensorResponse updateSensor(Long id, SensorRequest request);
    void deleteSensor(Long id);
}