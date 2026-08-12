package com.tw.medtech.pfa.web.dto;

import com.tw.medtech.pfa.model.enums.Role;

import java.util.List;

public record UserWithRoomsDto(
        Long id,
        String name,
        String email,
        int phoneNumber,
        List<Role> roles,
        List<RoomForUserDto> rooms
) {}