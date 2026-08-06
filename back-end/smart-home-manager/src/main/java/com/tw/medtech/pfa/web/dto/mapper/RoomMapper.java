package com.tw.medtech.pfa.web.dto.mapper;

import com.tw.medtech.pfa.dao.connectors.dto.SensorResponse;
import com.tw.medtech.pfa.model.Device;
import com.tw.medtech.pfa.model.Room;
import com.tw.medtech.pfa.model.Sensor;
import com.tw.medtech.pfa.web.dto.DeviceDto;
import com.tw.medtech.pfa.web.dto.RoomDto;
import com.tw.medtech.pfa.web.dto.UserDto;

import java.util.List;
import java.util.stream.Collectors;

public class RoomMapper {

    public static RoomDto mapToDto(Room room) {
        List<UserDto> users = room.getUsers().stream()
                .map(u -> new UserDto(u.getId(), u.getName(), u.getEmail()))
                .collect(Collectors.toList());

        List<DeviceDto> devices = room.getDevices().stream()
                .filter(hc -> hc instanceof Device)
                .map(hc -> (Device) hc)
                .map(d -> new DeviceDto(
                        d.getId(),
                        d.getName(),
                        d.getUnit() != null ? d.getUnit().toString() : null,
                        d.getStatus() != null ? d.getStatus().name() : null,
                        room.getId(),
                        room.getName()
                ))
                .collect(Collectors.toList());

        List<SensorResponse> sensors = room.getDevices().stream()
                .filter(hc -> hc instanceof Sensor)
                .map(hc -> (Sensor) hc)
                .map(s -> new SensorResponse(
                        s.getId(),
                        s.getName(),
                        s.getType(),
                        s.getUnit() != null ? s.getUnit().toString() : null,
                        s.getStatus(),
                        s.getData(),
                        room.getId().toString(),
                        room.getName()
                ))
                .collect(Collectors.toList());

        return new RoomDto(room.getId(), room.getName(), users, devices, sensors);
    }
}