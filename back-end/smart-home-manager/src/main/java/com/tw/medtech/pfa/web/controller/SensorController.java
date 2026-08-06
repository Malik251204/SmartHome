package com.tw.medtech.pfa.web.controller;

import com.tw.medtech.pfa.dao.connectors.dto.SensorResponse;
import com.tw.medtech.pfa.service.SensorService;
import lombok.RequiredArgsConstructor;
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
}