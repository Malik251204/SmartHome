package com.tw.medtech.pfa.dao.repository;

import com.tw.medtech.pfa.model.Sensor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SensorRepository extends JpaRepository<Sensor, Long> {
}
