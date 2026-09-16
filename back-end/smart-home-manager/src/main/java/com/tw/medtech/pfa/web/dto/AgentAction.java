package com.tw.medtech.pfa.web.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// Doubles as two things: (1) the LLM's raw proposal, deserialized
// straight from its JSON response — deviceId/currentStatus/newStatus/
// details/reasoning are populated that way; deviceName/previousStatus
// come back null since the model was never asked to state them; (2) the
// enriched, persisted record of what was actually applied, built
// server-side in AgentServiceImpl once a proposal survives validation —
// at that point deviceName/previousStatus get filled in from what we
// already know about the device, not from anything the model said.
// currentStatus is the model's own claim of the device's status before
// the change (useful to cross-check against previousStatus — a mismatch
// means the model misread the device state).
@JsonIgnoreProperties(ignoreUnknown = true)
public record AgentAction(
        Long deviceId,
        String deviceName,
        String previousStatus,
        String currentStatus,
        String newStatus,
        String details,
        String reasoning
) {}