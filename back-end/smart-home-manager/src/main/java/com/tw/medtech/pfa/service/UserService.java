package com.tw.medtech.pfa.service;

import com.tw.medtech.pfa.web.dto.UserRequest;
import com.tw.medtech.pfa.web.dto.UserWithRoomsDto;

import java.util.List;

public interface UserService {
    List<UserWithRoomsDto> getAllUsers();
    UserWithRoomsDto getUserWithRooms(Long id);
    UserWithRoomsDto createUser(UserRequest request);
    UserWithRoomsDto updateUser(Long id, UserRequest request);
    void deleteUser(Long id);
}