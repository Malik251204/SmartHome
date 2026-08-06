package com.tw.medtech.pfa.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "home_component")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class HomeComponent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Double unit;

    // Moved up from Device/Sensor, which each declared this identically.
    // Room.devices' `mappedBy = "room"` targets HomeComponent (the
    // collection's declared element type), so the field has to live here,
    // not on the subclasses, for Hibernate to resolve it.
    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

}
