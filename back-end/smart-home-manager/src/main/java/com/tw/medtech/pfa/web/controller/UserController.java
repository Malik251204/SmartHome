package com.tw.medtech.pfa.web.controller;

import com.tw.medtech.pfa.service.UserService;
import com.tw.medtech.pfa.web.dto.UserRequest;
import com.tw.medtech.pfa.web.dto.UserWithRoomsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public List<UserWithRoomsDto> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public UserWithRoomsDto getUserById(@PathVariable Long id) {
        return userService.getUserWithRooms(id);
    }

    @PostMapping
    public ResponseEntity<UserWithRoomsDto> createUser(@RequestBody UserRequest request) {
        return ResponseEntity.ok(userService.createUser(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserWithRoomsDto> updateUser(@PathVariable Long id, @RequestBody UserRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}