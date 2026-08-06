package com.tw.medtech.pfa.service;

import com.tw.medtech.pfa.web.dto.RoomDto;

import java.util.List;

public interface RoomService {
    List<RoomDto> getAllRooms();
    RoomDto getRoomById(Long id);
}