package com.tw.medtech.pfa.service.impl;

import com.tw.medtech.pfa.dao.connectors.SensorClient;
import com.tw.medtech.pfa.dao.repository.RoomRepository;
import com.tw.medtech.pfa.dao.repository.UserRepository;
import com.tw.medtech.pfa.exception.ResourceNotFoundException;
import com.tw.medtech.pfa.model.Room;
import com.tw.medtech.pfa.model.User;
import com.tw.medtech.pfa.service.RoomService;
import com.tw.medtech.pfa.service.SensorSeeder;
import com.tw.medtech.pfa.web.dto.RoomDto;
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
    private final UserRepository userRepository;
    private final RoomMapper roomMapper;
    private final SensorSeeder sensorSeeder;
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
        return roomMapper.mapToDto(findRoomOrThrow(id));
    }

    @Override
    @Transactional
    public RoomDto createRoom(String name) {
        // Saved first so it has an id (sensorIds' join table needs a real
        // room_id), then seeded, then saved again to persist those ids —
        // same "every room gets the same 3 ambient sensors" behavior
        // DataSeeder already gives the initial seed rooms, see SensorSeeder.
        Room saved = roomRepository.save(Room.builder().name(name).build());
        sensorSeeder.seedDefaultSensors(saved);
        Room withSensors = roomRepository.save(saved);
        return roomMapper.mapToDto(withSensors);
    }

    @Override
    @Transactional
    public RoomDto renameRoom(Long id, String name) {
        Room room = findRoomOrThrow(id);
        room.setName(name);
        return roomMapper.mapToDto(roomRepository.save(room));
    }

    @Override
    @Transactional
    public void deleteRoom(Long id) {
        // devices cascade with the room (CascadeType.ALL on
        // Room.devices); the room_users join rows go too, since Room owns
        // that relationship. sensorIds are just an @ElementCollection, so
        // that table's rows go as well. The mock backend's sensors
        // themselves are deleted explicitly here first — they're a
        // separate service with no cascade of its own, so without this
        // they'd keep running, orphaned.
        Room room = findRoomOrThrow(id);
        for (Long sensorId : room.getSensorIds()) {
            sensorClient.deleteSensor(sensorId);
        }
        roomRepository.delete(room);
    }

    @Override
    @Transactional
    public RoomDto assignUser(Long roomId, Long userId) {
        Room room = findRoomOrThrow(roomId);
        User user = findUserOrThrow(userId);
        if (!room.getUsers().contains(user)) {
            room.getUsers().add(user);
        }
        return roomMapper.mapToDto(roomRepository.save(room));
    }

    @Override
    @Transactional
    public RoomDto removeUser(Long roomId, Long userId) {
        Room room = findRoomOrThrow(roomId);
        User user = findUserOrThrow(userId);
        room.getUsers().remove(user);
        return roomMapper.mapToDto(roomRepository.save(room));
    }

    private Room findRoomOrThrow(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + id));
    }

    private User findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }
}