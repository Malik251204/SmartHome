package com.tw.medtech.pfa.service;

import com.tw.medtech.pfa.web.dto.RoomDto;
import com.tw.medtech.pfa.web.dto.RoomRequest;

import java.util.List;

public interface RoomService {
    List<RoomDto> getAllRooms();
    RoomDto getRoomById(Long id);
    RoomDto createRoom(RoomRequest request);
    RoomDto updateRoom(Long id, RoomRequest request);
    void deleteRoom(Long id);
}