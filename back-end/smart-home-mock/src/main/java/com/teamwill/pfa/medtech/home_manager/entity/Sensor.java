package com.teamwill.pfa.medtech.home_manager.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Mock-backend's live-data simulation for a room's ambient sensors (light,
// temperature, occupancy) — independent of whatever Devices (actuators)
// the actual backend has in that room. One table, one `type` column, no
// inheritance — the frontend gets a single /sensors endpoint.
//
// This is intentionally scoped to ONLY what simulating a sensor's live
// data requires. Room, User, and Preferences used to live in this backend
// too, but were removed once the project's real target architecture
// became clear: the actual (Postgres) backend already owns that business
// data for real — this backend's only job is to simulate readings for
// sensors the actual backend already knows about. See the handoff doc for
// the reasoning.
@Entity
@Table(name = "sensors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sensor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SensorType type;

    @Column(nullable = false)
    private String unit;

    @Column(nullable = false)
    private String status;

    // Raw JSON string, same convention as the real backend (frontend already
    // knows how to serialize/parse this per type).
    @Column(length = 2000)
    private String data;
}
