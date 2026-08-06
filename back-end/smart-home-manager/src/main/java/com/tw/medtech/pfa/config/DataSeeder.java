package com.tw.medtech.pfa.config;

import com.tw.medtech.pfa.dao.repository.DeviceRepository;
import com.tw.medtech.pfa.dao.repository.RoomRepository;
import com.tw.medtech.pfa.dao.repository.UserRepository;
import com.tw.medtech.pfa.model.Device;
import com.tw.medtech.pfa.model.Room;
import com.tw.medtech.pfa.model.User;
import com.tw.medtech.pfa.model.enums.DeviceStatus;
import com.tw.medtech.pfa.model.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final DeviceRepository deviceRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return; // already seeded, skip
        }

        User alice = User.builder().name("Alice Smith").email("alice@example.com")
                .phoneNumber(555123456).roles(List.of(Role.ADMIN)).build();
        User bob = User.builder().name("Bob Jones").email("bob@example.com")
                .phoneNumber(555987654).roles(List.of(Role.USER)).build();
        User carol = User.builder().name("Carol Lee").email("carol@example.com")
                .phoneNumber(555222333).roles(List.of(Role.USER)).build();
        userRepository.saveAll(List.of(alice, bob, carol));

        Room livingRoom = Room.builder().name("Living Room").build();
        Room bedroom = Room.builder().name("Bedroom").build();
        Room kitchen = Room.builder().name("Kitchen").build();

        livingRoom.getUsers().addAll(List.of(alice, bob));
        bedroom.getUsers().addAll(List.of(bob));
        kitchen.getUsers().addAll(List.of(alice, carol));

        roomRepository.saveAll(List.of(livingRoom, bedroom, kitchen));

        Device livingRoomLight = Device.builder()
                .name("Living Room Light").unit(1.0).status(DeviceStatus.ON).room(livingRoom).build();
        Device bedroomThermostat = Device.builder()
                .name("Bedroom Thermostat").unit(21.5).status(DeviceStatus.OFF).room(bedroom).build();
        Device kitchenPlug = Device.builder()
                .name("Kitchen Smart Plug").unit(1.0).status(DeviceStatus.ON).room(kitchen).build();
        deviceRepository.saveAll(List.of(livingRoomLight, bedroomThermostat, kitchenPlug));
    }
}