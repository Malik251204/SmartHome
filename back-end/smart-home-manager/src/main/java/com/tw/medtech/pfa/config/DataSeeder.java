package com.tw.medtech.pfa.config;

import com.tw.medtech.pfa.dao.repository.DeviceRepository;
import com.tw.medtech.pfa.dao.repository.RoomRepository;
import com.tw.medtech.pfa.dao.repository.UserRepository;
import com.tw.medtech.pfa.model.Device;
import com.tw.medtech.pfa.model.Room;
import com.tw.medtech.pfa.model.User;
import com.tw.medtech.pfa.model.enums.DeviceStatus;
import com.tw.medtech.pfa.model.enums.DeviceType;
import com.tw.medtech.pfa.model.enums.Role;
import com.tw.medtech.pfa.service.SensorSeeder;
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
    private final SensorSeeder sensorSeeder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() == 0) {
            seedUsersRoomsAndDevices();
        }

        // smart-home-mock's H2 is in-memory — wiped on ITS restart,
        // independent of whether Postgres (this app's own DB) got wiped.
        // Any sensorIds already stored on a Room are stale the moment
        // mock restarts without Postgres also being reset — those ids no
        // longer resolve to anything (SensorClient.getSensorById()
        // quietly returns null for them, RoomMapper filters them out, so
        // sensors just silently vanish from the room). Rather than
        // detecting staleness, just always clear + reseed every room's
        // sensors on every boot — sensors are meant to be
        // disposable/reseedable by design, unlike Users/Rooms/Devices,
        // which only seed once.
        List<Room> rooms = roomRepository.findAll();
        for (Room room : rooms) {
            room.getSensorIds().clear();
            sensorSeeder.seedDefaultSensors(room);
        }
        roomRepository.saveAll(rooms);
    }

    private void seedUsersRoomsAndDevices() {
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
    }
}