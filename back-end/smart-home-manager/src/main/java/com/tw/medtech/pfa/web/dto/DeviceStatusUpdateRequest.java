package com.tw.medtech.pfa.web.dto;

import com.tw.medtech.pfa.model.enums.DeviceStatus;

public record DeviceStatusUpdateRequest(DeviceStatus status) {}
