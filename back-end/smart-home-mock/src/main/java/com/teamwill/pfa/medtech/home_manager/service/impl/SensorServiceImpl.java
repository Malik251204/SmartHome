package com.teamwill.pfa.medtech.home_manager.service.impl;

import com.teamwill.pfa.medtech.home_manager.dto.ReadingDto;
import com.teamwill.pfa.medtech.home_manager.dto.SensorDto;
import com.teamwill.pfa.medtech.home_manager.entity.Reading;
import com.teamwill.pfa.medtech.home_manager.entity.Sensor;
import com.teamwill.pfa.medtech.home_manager.exception.ResourceNotFoundException;
import com.teamwill.pfa.medtech.home_manager.mapper.SensorMapper;
import com.teamwill.pfa.medtech.home_manager.repository.ReadingRepository;
import com.teamwill.pfa.medtech.home_manager.repository.SensorRepository;
import com.teamwill.pfa.medtech.home_manager.service.SensorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SensorServiceImpl implements SensorService {

    private final SensorRepository sensorRepository;
    private final ReadingRepository readingRepository;

    @Override
    public SensorDto createSensor(SensorDto dto) {
        Sensor saved = sensorRepository.save(SensorMapper.mapToEntity(dto));
        return SensorMapper.mapToDto(saved);
    }

    @Override
    public List<SensorDto> getAllSensors() {
        return sensorRepository.findAll()
                .stream()
                .map(SensorMapper::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public SensorDto getSensorById(Long id) {
        Sensor sensor = sensorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sensor not found with id: " + id));
        return SensorMapper.mapToDto(sensor);
    }

    @Override
    public SensorDto updateSensor(Long id, SensorDto dto) {
        Sensor existing = sensorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sensor not found with id: " + id));

        // PUT overwrites every field it's given — no command queue, this is
        // the only write path.
        existing.setName(dto.getName());
        existing.setUnit(dto.getUnit());
        existing.setStatus(dto.getStatus());
        existing.setData(dto.getData());

        return SensorMapper.mapToDto(sensorRepository.save(existing));
    }

    @Override
    public void deleteSensor(Long id) {
        if (!sensorRepository.existsById(id)) {
            throw new ResourceNotFoundException("Sensor not found with id: " + id);
        }
        sensorRepository.deleteById(id);
    }

    @Override
    public List<ReadingDto> getReadings(Long id, int limit) {
        return readingRepository.findTop50BySensorIdOrderByRecordedAtDesc(id)
                .stream()
                .limit(limit)
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private ReadingDto toDto(Reading r) {
        return ReadingDto.builder()
                .id(r.getId())
                .sensorId(r.getSensorId())
                .recordedAt(r.getRecordedAt())
                .data(r.getData())
                .build();
    }
}
