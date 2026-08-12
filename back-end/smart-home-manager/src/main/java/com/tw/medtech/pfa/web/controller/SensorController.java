package com.tw.medtech.pfa.web.controller;

import com.tw.medtech.pfa.dao.connectors.dto.SensorResponse;
import com.tw.medtech.pfa.service.SensorService;
import com.tw.medtech.pfa.web.dto.SensorRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sensors")
@RequiredArgsConstructor
public class SensorController {

    private final SensorService sensorService;

    @GetMapping
    public List<SensorResponse> getAllSensors() {
        return sensorService.getAllSensors();
    }

    @GetMapping("/{id}")
    public SensorResponse getSensorById(@PathVariable Long id) {
        return sensorService.getSensorById(id);
    }

    @PostMapping
    public ResponseEntity<SensorResponse> createSensor(@RequestBody SensorRequest request) {
        return ResponseEntity.ok(sensorService.createSensor(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SensorResponse> updateSensor(@PathVariable Long id, @RequestBody SensorRequest request) {
        return ResponseEntity.ok(sensorService.updateSensor(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSensor(@PathVariable Long id) {
        sensorService.deleteSensor(id);
        return ResponseEntity.noContent().build();
    }
}