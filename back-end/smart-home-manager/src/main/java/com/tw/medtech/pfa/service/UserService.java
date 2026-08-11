package com.tw.medtech.pfa.service;

import com.tw.medtech.pfa.web.dto.UserDto;
import com.tw.medtech.pfa.web.dto.UserRequest;

import java.util.List;

public interface UserService {
    List<UserDto> getAllUsers();
    UserDto getUserById(Long id);
    UserDto createUser(UserRequest request);
    UserDto updateUser(Long id, UserRequest request);
    void deleteUser(Long id);
}
