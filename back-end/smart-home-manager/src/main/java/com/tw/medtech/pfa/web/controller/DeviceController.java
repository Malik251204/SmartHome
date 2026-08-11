package com.tw.medtech.pfa.web.controller;

import com.tw.medtech.pfa.service.DeviceService;
import com.tw.medtech.pfa.web.dto.DeviceDto;
import com.tw.medtech.pfa.web.dto.DeviceStatusUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    // Status-only — Devices are the controllable actuators (AC/bulb/
    // curtains on-off-open-closed); everything else about a Device is
    // fixed at creation. Scoped this way on purpose, matching the rest of
    // this API's minimal surface.
    @PutMapping("/{id}/status")
    public DeviceDto updateStatus(@PathVariable Long id, @RequestBody DeviceStatusUpdateRequest request) {
        return deviceService.updateStatus(id, request.status());
    }
}
