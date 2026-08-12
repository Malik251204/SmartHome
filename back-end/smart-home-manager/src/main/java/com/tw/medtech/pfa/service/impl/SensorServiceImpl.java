package com.tw.medtech.pfa.service.impl;

import com.tw.medtech.pfa.dao.connectors.SensorClient;
import com.tw.medtech.pfa.dao.connectors.dto.MockSensorDto;
import com.tw.medtech.pfa.dao.connectors.dto.SensorResponse;
import com.tw.medtech.pfa.dao.repository.RoomRepository;
import com.tw.medtech.pfa.exception.ResourceNotFoundException;
import com.tw.medtech.pfa.model.Room;
import com.tw.medtech.pfa.service.SensorService;
import com.tw.medtech.pfa.web.dto.SensorRequest;
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

    @Override
    @Transactional
    public SensorResponse createSensor(SensorRequest request) {
        MockSensorDto toCreate = new MockSensorDto(
                null, request.name(), request.type(), request.unit(), request.status(), request.data()
        );
        MockSensorDto created = sensorClient.createSensor(toCreate);

        Room room = null;
        if (request.roomId() != null) {
            room = roomRepository.findById(request.roomId())
                    .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + request.roomId()));
            room.getSensorIds().add(created.id());
            roomRepository.save(room);
        }

        return toResponse(created, room);
    }

    @Override
    @Transactional
    public SensorResponse updateSensor(Long id, SensorRequest request) {
        MockSensorDto toUpdate = new MockSensorDto(
                id, request.name(), request.type(), request.unit(), request.status(), request.data()
        );
        MockSensorDto updated = sensorClient.updateSensor(id, toUpdate);
        if (updated == null) {
            throw new ResourceNotFoundException("Sensor not found with id: " + id);
        }
        Room room = findRoomForSensor(id, roomRepository.findAll());
        return toResponse(updated, room);
    }

    @Override
    @Transactional
    public void deleteSensor(Long id) {
        sensorClient.deleteSensor(id);
        // Remove the stale reference from whichever room held it
        roomRepository.findAll().stream()
                .filter(r -> r.getSensorIds().contains(id))
                .findFirst()
                .ifPresent(r -> {
                    r.getSensorIds().remove(id);
                    roomRepository.save(r);
                });
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