package com.tw.medtech.pfa.service;

import com.tw.medtech.pfa.model.enums.DeviceStatus;
import com.tw.medtech.pfa.web.dto.DeviceDto;

public interface DeviceService {
    DeviceDto updateStatus(Long id, DeviceStatus status);
}
