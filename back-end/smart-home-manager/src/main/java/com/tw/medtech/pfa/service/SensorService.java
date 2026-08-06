package com.tw.medtech.pfa.service;

import com.tw.medtech.pfa.dao.connectors.dto.SensorResponse;

import java.util.List;

public interface SensorService {
    List<SensorResponse> getAllSensors();
    SensorResponse getSensorById(Long id);
}