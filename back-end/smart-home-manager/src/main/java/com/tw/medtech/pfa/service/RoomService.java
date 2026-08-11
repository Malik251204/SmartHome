package com.tw.medtech.pfa.service;

import com.tw.medtech.pfa.web.dto.RoomDto;

import java.util.List;

public interface RoomService {
    List<RoomDto> getAllRooms();
    RoomDto getRoomById(Long id);
    RoomDto createRoom(String name);
    RoomDto renameRoom(Long id, String name);
    void deleteRoom(Long id);
    RoomDto assignUser(Long roomId, Long userId);
    RoomDto removeUser(Long roomId, Long userId);
}