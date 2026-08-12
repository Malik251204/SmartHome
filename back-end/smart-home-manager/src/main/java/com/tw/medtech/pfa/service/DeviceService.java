package com.tw.medtech.pfa.service;

import com.tw.medtech.pfa.web.dto.DeviceDto;
import com.tw.medtech.pfa.web.dto.DeviceRequest;

import java.util.List;

public interface DeviceService {
    List<DeviceDto> getAllDevices();
    DeviceDto getDeviceById(Long id);
    DeviceDto createDevice(DeviceRequest request);
    DeviceDto updateDevice(Long id, DeviceRequest request);
    void deleteDevice(Long id);
}