package com.tw.medtech.pfa.web.controller;

import com.tw.medtech.pfa.service.DeviceService;
import com.tw.medtech.pfa.web.dto.DeviceDto;
import com.tw.medtech.pfa.web.dto.DeviceRequest;
import com.tw.medtech.pfa.web.dto.DeviceStatusUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    // Status toggle — any signed-in user, unchanged (see SecurityConfig).
    // Kept separate from the full-edit endpoints below on purpose: this is
    // the narrow, frequent action ("turn the AC on"), those are the
    // occasional admin action ("this room got a new bulb").
    @PutMapping("/{id}/status")
    public DeviceDto updateStatus(@PathVariable Long id, @RequestBody DeviceStatusUpdateRequest request) {
        return deviceService.updateStatus(id, request.status());
    }

    @GetMapping
    public List<DeviceDto> getAllDevices() {
        return deviceService.getAllDevices();
    }

    @GetMapping("/{id}")
    public DeviceDto getDeviceById(@PathVariable Long id) {
        return deviceService.getDeviceById(id);
    }

    @PostMapping
    public ResponseEntity<DeviceDto> createDevice(@RequestBody DeviceRequest request) {
        return ResponseEntity.ok(deviceService.createDevice(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DeviceDto> updateDevice(@PathVariable Long id, @RequestBody DeviceRequest request) {
        return ResponseEntity.ok(deviceService.updateDevice(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDevice(@PathVariable Long id) {
        deviceService.deleteDevice(id);
        return ResponseEntity.noContent().build();
    }
}
