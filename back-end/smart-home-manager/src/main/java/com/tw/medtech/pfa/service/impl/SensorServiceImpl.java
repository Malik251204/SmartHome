package com.tw.medtech.pfa.service.impl;

import com.tw.medtech.pfa.dao.connectors.dto.SensorResponse;
import com.tw.medtech.pfa.dao.repository.SensorRepository;
import com.tw.medtech.pfa.exception.ResourceNotFoundException;
import com.tw.medtech.pfa.model.Room;
import com.tw.medtech.pfa.model.Sensor;
import com.tw.medtech.pfa.service.SensorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SensorServiceImpl implements SensorService {

    private final SensorRepository sensorRepository;

    @Override
    @Transactional(readOnly = true)
    public List<SensorResponse> getAllSensors() {
        return sensorRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SensorResponse getSensorById(Long id) {
        Sensor sensor = sensorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sensor not found with id: " + id));
        return mapToResponse(sensor);
    }

    private SensorResponse mapToResponse(Sensor sensor) {
        Room room = sensor.getRoom();
        return new SensorResponse(
                sensor.getId(),
                sensor.getName(),
                sensor.getType(),
                sensor.getUnit() != null ? sensor.getUnit().toString() : null,
                sensor.getStatus(),
                sensor.getData(),
                room != null ? room.getId().toString() : null,
                room != null ? room.getName() : null
        );
    }
}