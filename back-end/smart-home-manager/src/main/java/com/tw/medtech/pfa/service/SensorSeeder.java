package com.tw.medtech.pfa.service;

import com.tw.medtech.pfa.dao.connectors.SensorClient;
import com.tw.medtech.pfa.dao.connectors.dto.MockSensorDto;
import com.tw.medtech.pfa.model.Room;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// Every room gets the same fixed set of 3 ambient sensors (light,
// temperature, occupancy) — independent of whatever Devices it has. Used
// both at boot (DataSeeder, for the initial seed rooms) and whenever a
// room is created afterward (RoomServiceImpl), so both paths stay
// consistent instead of duplicating this logic.
@Component
@RequiredArgsConstructor
public class SensorSeeder {

    private final SensorClient sensorClient;

    public void seedDefaultSensors(Room room) {
        seedSensor(room, room.getName() + " Light Level", "LUX", "{\"lux\":400}");
        seedSensor(room, room.getName() + " Temperature", "TEMPERATURE", "{\"celsius\":21}");
        seedSensor(room, room.getName() + " Occupancy", "OCCUPANCY", "{\"count\":0}");
    }

    private void seedSensor(Room room, String name, String type, String initialData) {
        MockSensorDto toCreate = new MockSensorDto(null, name, type, "1.0", "on", initialData);
        MockSensorDto created = sensorClient.createSensor(toCreate);
        room.getSensorIds().add(created.id());
    }
}
