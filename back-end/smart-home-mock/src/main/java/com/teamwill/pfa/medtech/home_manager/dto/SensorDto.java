package com.teamwill.pfa.medtech.home_manager.dto;

import com.teamwill.pfa.medtech.home_manager.entity.SensorType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SensorDto {
    private Long id;
    private String name;
    private SensorType type;
    private String unit;
    private String status;
    private String data;
}
