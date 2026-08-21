package com.tw.medtech.pfa.service.impl;

import com.tw.medtech.pfa.dao.repository.RoomRepository;
import com.tw.medtech.pfa.dao.repository.UserRepository;
import com.tw.medtech.pfa.exception.ResourceNotFoundException;
import com.tw.medtech.pfa.model.Room;
import com.tw.medtech.pfa.model.User;
import com.tw.medtech.pfa.service.UserService;
import com.tw.medtech.pfa.web.dto.UserDto;
import com.tw.medtech.pfa.web.dto.UserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserById(Long id) {
        return toDto(findUserOrThrow(id));
    }

    @Override
    @Transactional
    public UserDto createUser(UserRequest request) {
        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .phoneNumber(request.phoneNumber())
                .roles(request.roles())
                .build();
        return toDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserDto updateUser(Long id, UserRequest request) {
        User user = findUserOrThrow(id);
        user.setName(request.name());
        user.setEmail(request.email());
        if (request.password() != null && !request.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }
        user.setPhoneNumber(request.phoneNumber());
        user.setRoles(request.roles());
        return toDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = findUserOrThrow(id);

        // Room owns the room_users relationship — deleting a user still
        // referenced there would violate the FK constraint, so detach
        // first from every room that has them.
        List<Room> rooms = roomRepository.findByUsersContaining(user);
        for (Room room : rooms) {
            room.getUsers().remove(user);
        }
        roomRepository.saveAll(rooms);

        userRepository.delete(user);
    }

    private User findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    private UserDto toDto(User user) {
        return new UserDto(user.getId(), user.getName(), user.getEmail(), user.getPhoneNumber(), user.getRoles());
    }
}
