package com.tw.medtech.pfa.service.impl;

import com.tw.medtech.pfa.dao.connectors.SensorClient;
import com.tw.medtech.pfa.dao.connectors.dto.MockSensorDto;
import com.tw.medtech.pfa.dao.connectors.dto.SensorResponse;
import com.tw.medtech.pfa.dao.repository.RoomRepository;
import com.tw.medtech.pfa.exception.ResourceNotFoundException;
import com.tw.medtech.pfa.model.Room;
import com.tw.medtech.pfa.service.SensorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SensorServiceImpl implements SensorService {

    private final SensorClient sensorClient;
    private final RoomRepository roomRepository;

    @Override
    @Transactional(readOnly = true)
    public List<SensorResponse> getAllSensors() {
        List<MockSensorDto> mockSensors = sensorClient.getAllSensors();
        List<Room> allRooms = roomRepository.findAll();

        return mockSensors.stream()
                .map(s -> toResponse(s, findRoomForSensor(s.id(), allRooms)))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SensorResponse getSensorById(Long id) {
        MockSensorDto sensor = sensorClient.getSensorById(id);
        if (sensor == null) {
            throw new ResourceNotFoundException("Sensor not found with id: " + id);
        }
        Room room = findRoomForSensor(id, roomRepository.findAll());
        return toResponse(sensor, room);
    }

    private Room findRoomForSensor(Long sensorId, List<Room> rooms) {
        return rooms.stream()
                .filter(r -> r.getSensorIds().contains(sensorId))
                .findFirst()
                .orElse(null);
    }

    private SensorResponse toResponse(MockSensorDto sensor, Room room) {
        return new SensorResponse(
                sensor.id(),
                sensor.name(),
                sensor.type(),
                sensor.unit(),
                sensor.status(),
                sensor.data(),
                room != null ? room.getId().toString() : null,
                room != null ? room.getName() : null
        );
    }
}