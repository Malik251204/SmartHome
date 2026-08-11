package com.tw.medtech.pfa.config;

import com.tw.medtech.pfa.dao.connectors.SensorClient;
import com.tw.medtech.pfa.dao.connectors.dto.MockSensorDto;
import com.tw.medtech.pfa.dao.repository.DeviceRepository;
import com.tw.medtech.pfa.dao.repository.RoomRepository;
import com.tw.medtech.pfa.dao.repository.UserRepository;
import com.tw.medtech.pfa.model.Device;
import com.tw.medtech.pfa.model.Room;
import com.tw.medtech.pfa.model.User;
import com.tw.medtech.pfa.model.enums.DeviceStatus;
import com.tw.medtech.pfa.model.enums.DeviceType;
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
    private final SensorClient sensorClient;

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

        Device livingRoomAc = Device.builder()
                .name("Living Room AC").unit(1.0).type(DeviceType.AC).status(DeviceStatus.ON).room(livingRoom).build();
        Device bedroomBulb = Device.builder()
                .name("Bedroom Bulb").unit(1.0).type(DeviceType.LIGHT_BULB).status(DeviceStatus.OFF).room(bedroom).build();
        Device kitchenCurtains = Device.builder()
                .name("Kitchen Curtains").unit(1.0).type(DeviceType.CURTAINS).status(DeviceStatus.ON).room(kitchen).build();
        deviceRepository.saveAll(List.of(livingRoomAc, bedroomBulb, kitchenCurtains));
// Every room gets all 3 sensor types, regardless of its devices/users.
        for (Room room : List.of(livingRoom, bedroom, kitchen)) {
            seedSensorForRoom(room, room.getName() + " AC Sensor", "AC", "{\"mode\":\"COOL\",\"targetTemp\":22}");
            seedSensorForRoom(room, room.getName() + " Bulb Sensor", "LIGHT_BULB", "{\"isOn\":true,\"brightness\":80}");
            seedSensorForRoom(room, room.getName() + " Curtains Sensor", "CURTAINS", "{\"roomLightLux\":400}");
        }

        roomRepository.saveAll(List.of(livingRoom, bedroom, kitchen));
    }

    private void seedSensorForRoom(Room room, String name, String type, String data) {
        MockSensorDto toCreate = new MockSensorDto(null, name, type, "1.0", "ON", data);
        MockSensorDto created = sensorClient.createSensor(toCreate);
        room.getSensorIds().add(created.id());
    }



}