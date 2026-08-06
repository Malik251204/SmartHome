package com.tw.medtech.pfa.dao.repository;

import com.tw.medtech.pfa.model.Device;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceRepository extends JpaRepository<Device, Long> {
}
