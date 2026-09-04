package com.tw.medtech.pfa.web.dto;

// A human-readable sensor reading, e.g. {"type":"TEMPERATURE","label":
// "Temperature","value":"21.7 °C"} — replaces the raw JSON blob that
// used to be the only record of what the agent actually saw, so a
// decision can be read without parsing a nested JSON string by hand.
public record SensorDetail(
        String type,
        String label,
        String value
) {}
