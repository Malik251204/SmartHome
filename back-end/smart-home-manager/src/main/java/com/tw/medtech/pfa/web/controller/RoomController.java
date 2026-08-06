package com.tw.medtech.pfa.web.controller;

import com.tw.medtech.pfa.service.RoomService;
import com.tw.medtech.pfa.web.dto.RoomDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @GetMapping
    public List<RoomDto> getAllRooms() {
        return roomService.getAllRooms();
    }

    @GetMapping("/{id}")
    public RoomDto getRoomById(@PathVariable Long id) {
        return roomService.getRoomById(id);
    }
}