package com.tw.medtech.pfa.service.impl;

import com.tw.medtech.pfa.dao.repository.DeviceRepository;
import com.tw.medtech.pfa.dao.repository.RoomRepository;
import com.tw.medtech.pfa.exception.ResourceNotFoundException;
import com.tw.medtech.pfa.model.Device;
import com.tw.medtech.pfa.model.Room;
import com.tw.medtech.pfa.model.enums.DeviceStatus;
import com.tw.medtech.pfa.model.enums.DeviceType;
import com.tw.medtech.pfa.service.DeviceService;
import com.tw.medtech.pfa.web.dto.DeviceDto;
import com.tw.medtech.pfa.web.dto.DeviceRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeviceServiceImpl implements DeviceService {

    private final DeviceRepository deviceRepository;
    private final RoomRepository roomRepository;

    @Override
    @Transactional
    public DeviceDto updateStatus(Long id, DeviceStatus status) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with id: " + id));

        device.setStatus(status);
        Device saved = deviceRepository.save(device);
        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeviceDto> getAllDevices() {
        return deviceRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DeviceDto getDeviceById(Long id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with id: " + id));
        return toDto(device);
    }

    @Override
    @Transactional
    public DeviceDto createDevice(DeviceRequest request) {
        Room room = roomRepository.findById(request.roomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + request.roomId()));

        Device device = Device.builder()
                .name(request.name())
                .type(request.type() != null ? DeviceType.valueOf(request.type()) : null)
                .unit(request.unit())
                .status(DeviceStatus.valueOf(request.status()))
                .room(room)
                .build();

        Device saved = deviceRepository.save(device);
        return toDto(saved);
    }

    @Override
    @Transactional
    public DeviceDto updateDevice(Long id, DeviceRequest request) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with id: " + id));

        device.setName(request.name());
        device.setType(request.type() != null ? DeviceType.valueOf(request.type()) : null);
        device.setUnit(request.unit());
        device.setStatus(DeviceStatus.valueOf(request.status()));

        if (request.roomId() != null && !request.roomId().equals(device.getRoom().getId())) {
            Room newRoom = roomRepository.findById(request.roomId())
                    .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + request.roomId()));
            device.setRoom(newRoom);
        }

        Device saved = deviceRepository.save(device);
        return toDto(saved);
    }

    @Override
    @Transactional
    public void deleteDevice(Long id) {
        if (!deviceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Device not found with id: " + id);
        }
        deviceRepository.deleteById(id);
    }

    private DeviceDto toDto(Device d) {
        return new DeviceDto(
                d.getId(),
                d.getName(),
                d.getType() != null ? d.getType().name() : null,
                d.getUnit() != null ? d.getUnit().toString() : null,
                d.getStatus() != null ? d.getStatus().name() : null,
                d.getRoom() != null ? d.getRoom().getId() : null,
                d.getRoom() != null ? d.getRoom().getName() : null
        );
    }
}
