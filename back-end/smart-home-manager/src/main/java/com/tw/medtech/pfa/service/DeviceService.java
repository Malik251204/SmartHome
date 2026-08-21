package com.tw.medtech.pfa.service;

import com.tw.medtech.pfa.model.enums.DeviceStatus;
import com.tw.medtech.pfa.web.dto.DeviceDto;
import com.tw.medtech.pfa.web.dto.DeviceRequest;

import java.util.List;

public interface DeviceService {
    DeviceDto updateStatus(Long id, DeviceStatus status);

    // Full management (create/edit/remove a room's devices) — admin-only,
    // separate from the status toggle above which any signed-in user can
    // do on their own room's devices.
    List<DeviceDto> getAllDevices();
    DeviceDto getDeviceById(Long id);
    DeviceDto createDevice(DeviceRequest request);
    DeviceDto updateDevice(Long id, DeviceRequest request);
    void deleteDevice(Long id);
}
