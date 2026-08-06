package com.teamwill.pfa.medtech.home_manager.controller;

import com.teamwill.pfa.medtech.home_manager.dto.ReadingDto;
import com.teamwill.pfa.medtech.home_manager.dto.SensorDto;
import com.teamwill.pfa.medtech.home_manager.service.SensorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// One unified endpoint for all three sensor types (type is a real field on
// the payload), replacing the real backend's split /devices/ac, /bulbs,
// /curtains resources. Updates are direct and synchronous — no command
// queue, no PENDING/EXECUTED/FAILED lifecycle.
@RestController
@RequestMapping("/api/sensors")
@RequiredArgsConstructor
public class SensorController {

    private final SensorService sensorService;

    @PostMapping
    public ResponseEntity<SensorDto> createSensor(@RequestBody SensorDto dto) {
        return ResponseEntity.ok(sensorService.createSensor(dto));
    }

    @GetMapping
    public ResponseEntity<List<SensorDto>> getAllSensors() {
        return ResponseEntity.ok(sensorService.getAllSensors());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SensorDto> getSensorById(@PathVariable Long id) {
        return ResponseEntity.ok(sensorService.getSensorById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SensorDto> updateSensor(@PathVariable Long id, @RequestBody SensorDto dto) {
        return ResponseEntity.ok(sensorService.updateSensor(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSensor(@PathVariable Long id) {
        sensorService.deleteSensor(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/readings")
    public ResponseEntity<List<ReadingDto>> getReadings(
            @PathVariable Long id,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(sensorService.getReadings(id, limit));
    }
}
