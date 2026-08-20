package com.tw.medtech.pfa.service.impl;

import com.tw.medtech.pfa.dao.connectors.SensorClient;
import com.tw.medtech.pfa.dao.connectors.dto.MockSensorDto;
import com.tw.medtech.pfa.dao.repository.RoomRepository;
import com.tw.medtech.pfa.exception.ResourceNotFoundException;
import com.tw.medtech.pfa.model.Room;
import com.tw.medtech.pfa.service.RoomService;
import com.tw.medtech.pfa.web.dto.RoomDto;
import com.tw.medtech.pfa.web.dto.RoomRequest;
import com.tw.medtech.pfa.web.dto.mapper.RoomMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final RoomMapper roomMapper;
    private final SensorClient sensorClient;

    @Override
    @Transactional(readOnly = true)
    public List<RoomDto> getAllRooms() {
        return roomRepository.findAll().stream()
                .map(roomMapper::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public RoomDto getRoomById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + id));
        return roomMapper.mapToDto(room);
    }

    @Override
    @Transactional
    public RoomDto createRoom(RoomRequest request) {
        Room room = Room.builder().name(request.name()).build();
        Room saved = roomRepository.save(room);

        // Every new room ships with all 3 sensor types, seeded in the mock
        // backend with starting data that keeps them actively drifting
        // (see SensorSimulationScheduler's drift conditions).
        seedSensor(saved, "AC", saved.getName() + " AC Sensor", "{\"mode\":\"COOL\",\"targetTemp\":22}");
        seedSensor(saved, "LIGHT_BULB", saved.getName() + " Bulb Sensor", "{\"isOn\":true,\"brightness\":80}");
        seedSensor(saved, "CURTAINS", saved.getName() + " Curtains Sensor", "{\"roomLightLux\":400}");

        Room withSensors = roomRepository.save(saved);
        return roomMapper.mapToDto(withSensors);
    }

    @Override
    @Transactional
    public RoomDto updateRoom(Long id, RoomRequest request) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + id));
        room.setName(request.name());
        Room saved = roomRepository.save(room);
        return roomMapper.mapToDto(saved);
    }

    @Override
    @Transactional
    public void deleteRoom(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + id));

        for (Long sensorId : room.getSensorIds()) {
            sensorClient.deleteSensor(sensorId);
        }

        roomRepository.deleteById(id);
    }
    private void seedSensor(Room room, String type, String name, String data) {
        MockSensorDto toCreate = new MockSensorDto(null, name, type, "1.0", "ON", data);
        MockSensorDto created = sensorClient.createSensor(toCreate);
        room.getSensorIds().add(created.id());
    }
}