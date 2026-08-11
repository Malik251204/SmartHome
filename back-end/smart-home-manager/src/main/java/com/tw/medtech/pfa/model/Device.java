package com.tw.medtech.pfa.model;

import com.tw.medtech.pfa.model.enums.DeviceStatus;
import com.tw.medtech.pfa.model.enums.DeviceType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Data
@NoArgsConstructor
@SuperBuilder
public class Device extends HomeComponent {

    // type/status share the "type"/"status" columns with Sensor (single
    // table inheritance on HomeComponent). Sensor's are Strings, so these
    // must be @Enumerated(STRING) too, or Hibernate defaults to ORDINAL
    // (numeric) and the two subclasses fight over the column's SQL type.
    @Enumerated(EnumType.STRING)
    @Column
    private DeviceType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeviceStatus status;
}