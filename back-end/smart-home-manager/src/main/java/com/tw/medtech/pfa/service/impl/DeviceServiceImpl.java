package com.tw.medtech.pfa.service.impl;

import com.tw.medtech.pfa.dao.repository.DeviceRepository;
import com.tw.medtech.pfa.exception.ResourceNotFoundException;
import com.tw.medtech.pfa.model.Device;
import com.tw.medtech.pfa.model.enums.DeviceStatus;
import com.tw.medtech.pfa.service.DeviceService;
import com.tw.medtech.pfa.web.dto.DeviceDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeviceServiceImpl implements DeviceService {

    private final DeviceRepository deviceRepository;

    @Override
    @Transactional
    public DeviceDto updateStatus(Long id, DeviceStatus status) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with id: " + id));

        device.setStatus(status);
        Device saved = deviceRepository.save(device);

        return new DeviceDto(
                saved.getId(),
                saved.getName(),
                saved.getType() != null ? saved.getType().name() : null,
                saved.getUnit() != null ? saved.getUnit().toString() : null,
                saved.getStatus() != null ? saved.getStatus().name() : null,
                saved.getRoom() != null ? saved.getRoom().getId() : null,
                saved.getRoom() != null ? saved.getRoom().getName() : null
        );
    }
}
