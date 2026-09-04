package com.tw.medtech.pfa.web.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// Doubles as two things: (1) the LLM's raw proposal, deserialized
// straight from its JSON response — only deviceId/newStatus/reasoning
// are ever populated that way, deviceName/previousStatus come back
// null since the model was never asked to state them; (2) the enriched,
// persisted record of what was actually applied, built server-side in
// AgentServiceImpl once a proposal survives validation — at that point
// deviceName/previousStatus get filled in from what we already know
// about the device, not from anything the model said.
@JsonIgnoreProperties(ignoreUnknown = true)
public record AgentAction(
        Long deviceId,
        String deviceName,
        String previousStatus,
        String newStatus,
        String reasoning
) {}
