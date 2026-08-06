package com.teamwill.pfa.medtech.home_manager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReadingDto {
    private Long id;
    private Long sensorId;
    private Instant recordedAt;
    private String data;
}
