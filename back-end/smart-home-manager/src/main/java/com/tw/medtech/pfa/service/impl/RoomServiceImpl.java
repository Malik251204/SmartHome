package com.tw.medtech.pfa.service.impl;

import com.tw.medtech.pfa.dao.repository.RoomRepository;
import com.tw.medtech.pfa.exception.ResourceNotFoundException;
import com.tw.medtech.pfa.model.Room;
import com.tw.medtech.pfa.service.RoomService;
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

    @Override
    @Transactional(readOnly = true)
    public List<RoomDto> getAllRooms() {
        return roomRepository.findAll().stream()
                .map(RoomMapper::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public RoomDto getRoomById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + id));
        return RoomMapper.mapToDto(room);
    }
}