package com.tw.medtech.pfa.model;

import com.tw.medtech.pfa.model.enums.DeviceStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;

@Entity
@Data
@NoArgsConstructor
@SuperBuilder
public class Device extends HomeComponent{

    @Column(nullable = false)
    private  DeviceStatus status;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;
}
