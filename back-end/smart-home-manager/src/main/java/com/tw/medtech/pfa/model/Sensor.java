package com.tw.medtech.pfa.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Sensor extends HomeComponent {

    @Column
    private String type;   // e.g. "TEMPERATURE", "MOTION", "LIGHT"

    @Column
    private String status;  // e.g. "ACTIVE", "INACTIVE"

    @Column(length = 2000)
    private String data;    // raw JSON string, e.g. {"isOpen":false,"roomLightLux":363}
}