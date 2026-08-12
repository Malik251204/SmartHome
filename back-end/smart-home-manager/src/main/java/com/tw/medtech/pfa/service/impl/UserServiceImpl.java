package com.tw.medtech.pfa.service.impl;

import com.tw.medtech.pfa.dao.repository.RoomRepository;
import com.tw.medtech.pfa.dao.repository.UserRepository;
import com.tw.medtech.pfa.exception.ResourceNotFoundException;
import com.tw.medtech.pfa.model.User;
import com.tw.medtech.pfa.service.UserService;
import com.tw.medtech.pfa.web.dto.RoomForUserDto;
import com.tw.medtech.pfa.web.dto.UserRequest;
import com.tw.medtech.pfa.web.dto.UserWithRoomsDto;
import com.tw.medtech.pfa.web.dto.mapper.RoomMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final RoomMapper roomMapper;

    @Override
    @Transactional(readOnly = true)
    public List<UserWithRoomsDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserWithRoomsDto getUserWithRooms(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return toDto(user);
    }

    @Override
    @Transactional
    public UserWithRoomsDto createUser(UserRequest request) {
        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .phoneNumber(request.phoneNumber())
                .roles(request.roles())
                .build();
        User saved = userRepository.save(user);
        return toDto(saved);
    }

    @Override
    @Transactional
    public UserWithRoomsDto updateUser(Long id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPhoneNumber(request.phoneNumber());
        user.setRoles(request.roles());
        User saved = userRepository.save(user);
        return toDto(saved);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    private UserWithRoomsDto toDto(User user) {
        List<RoomForUserDto> rooms = roomRepository.findByUsers_Id(user.getId()).stream()
                .map(roomMapper::mapToRoomForUserDto)
                .collect(Collectors.toList());

        return new UserWithRoomsDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getRoles(),
                rooms
        );
    }
}