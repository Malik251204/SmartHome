package com.teamwill.pfa.medtech.home_manager.repository;

import com.teamwill.pfa.medtech.home_manager.entity.Sensor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SensorRepository extends JpaRepository<Sensor, Long> {
}
