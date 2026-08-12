package com.tw.medtech.pfa.web.dto.mapper;

import com.tw.medtech.pfa.dao.connectors.SensorClient;
import com.tw.medtech.pfa.dao.connectors.dto.MockSensorDto;
import com.tw.medtech.pfa.dao.connectors.dto.SensorResponse;
import com.tw.medtech.pfa.model.Device;
import com.tw.medtech.pfa.model.Room;
import com.tw.medtech.pfa.web.dto.DeviceDto;
import com.tw.medtech.pfa.web.dto.RoomDto;
import com.tw.medtech.pfa.web.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.tw.medtech.pfa.web.dto.RoomForUserDto;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RoomMapper {

    private final SensorClient sensorClient;

    public RoomDto mapToDto(Room room) {
        List<UserDto> users = room.getUsers().stream()
                .map(u -> new UserDto(u.getId(), u.getName(), u.getEmail(), u.getPhoneNumber(), u.getRoles()))
                .collect(Collectors.toList());

        List<DeviceDto> devices = room.getDevices().stream()
                .filter(hc -> hc instanceof Device)
                .map(hc -> (Device) hc)
                .map(d -> new DeviceDto(
                        d.getId(),
                        d.getName(),
                        d.getType() != null ? d.getType().name() : null,
                        d.getUnit() != null ? d.getUnit().toString() : null,
                        d.getStatus() != null ? d.getStatus().name() : null,
                        room.getId(),
                        room.getName()
                ))
                .collect(Collectors.toList());

        List<SensorResponse> sensors = room.getSensorIds().stream()
                .map(sensorClient::getSensorById)
                .filter(java.util.Objects::nonNull)
                .map(s -> toSensorResponse(s, room))
                .collect(Collectors.toList());

        return new RoomDto(room.getId(), room.getName(), users, devices, sensors);
    }

    private SensorResponse toSensorResponse(MockSensorDto sensor, Room room) {
        return new SensorResponse(
                sensor.id(),
                sensor.name(),
                sensor.type(),
                sensor.unit(),
                sensor.status(),
                sensor.data(),
                room.getId().toString(),
                room.getName()
        );
    }
    public RoomForUserDto mapToRoomForUserDto(Room room) {
        List<DeviceDto> devices = room.getDevices().stream()
                .filter(hc -> hc instanceof Device)
                .map(hc -> (Device) hc)
                .map(d -> new DeviceDto(
                        d.getId(),
                        d.getName(),
                        d.getType() != null ? d.getType().name() : null,
                        d.getUnit() != null ? d.getUnit().toString() : null,
                        d.getStatus() != null ? d.getStatus().name() : null,
                        room.getId(),
                        room.getName()
                ))
                .collect(Collectors.toList());

        List<SensorResponse> sensors = room.getSensorIds().stream()
                .map(sensorClient::getSensorById)
                .filter(java.util.Objects::nonNull)
                .map(s -> toSensorResponse(s, room))
                .collect(Collectors.toList());

        return new RoomForUserDto(room.getId(), room.getName(), devices, sensors);
    }
}