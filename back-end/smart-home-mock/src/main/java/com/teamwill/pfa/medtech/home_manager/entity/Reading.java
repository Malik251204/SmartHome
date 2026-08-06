package com.teamwill.pfa.medtech.home_manager.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

// A point-in-time snapshot of a sensor's simulated data. Appended (not
// overwritten) every tick of the simulation scheduler, unlike Sensor.data
// itself which only ever holds the latest value. Feeds the frontend's
// sparkline history.
@Entity
@Table(name = "readings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sensor_id", nullable = false)
    private Long sensorId;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    @Column(length = 2000)
    private String data;
}
