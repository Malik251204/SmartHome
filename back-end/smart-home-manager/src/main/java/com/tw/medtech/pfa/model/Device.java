package com.tw.medtech.pfa.model;

import com.tw.medtech.pfa.model.enums.DeviceStatus;
import com.tw.medtech.pfa.model.enums.DeviceType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Data
@NoArgsConstructor
@SuperBuilder
public class Device extends HomeComponent {

    @Column
    private DeviceType type;

    @Column(nullable = false)
    private DeviceStatus status;
}