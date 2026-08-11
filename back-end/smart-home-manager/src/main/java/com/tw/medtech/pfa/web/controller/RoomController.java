package com.tw.medtech.pfa.web.controller;

import com.tw.medtech.pfa.service.RoomService;
import com.tw.medtech.pfa.web.dto.RoomCreateRequest;
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

    @PostMapping
    public RoomDto createRoom(@RequestBody RoomCreateRequest request) {
        return roomService.createRoom(request.name());
    }

    @PutMapping("/{id}")
    public RoomDto renameRoom(@PathVariable Long id, @RequestBody RoomCreateRequest request) {
        return roomService.renameRoom(id, request.name());
    }

    @DeleteMapping("/{id}")
    public void deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
    }

    @PostMapping("/{roomId}/users/{userId}")
    public RoomDto assignUser(@PathVariable Long roomId, @PathVariable Long userId) {
        return roomService.assignUser(roomId, userId);
    }

    @DeleteMapping("/{roomId}/users/{userId}")
    public RoomDto removeUser(@PathVariable Long roomId, @PathVariable Long userId) {
        return roomService.removeUser(roomId, userId);
    }
}