package com.tw.medtech.pfa.service;

import com.tw.medtech.pfa.web.dto.AgentDecisionDto;
import com.tw.medtech.pfa.web.dto.AgentRunSummary;

import java.util.List;

public interface AgentService {
    // Evaluates every enabled preference against its room(s), applies any
    // resulting device changes, and persists an AgentDecision per
    // (preference, room) pair evaluated. Called both by the scheduled tick
    // and by the manual trigger endpoint — same method, same behavior.
    AgentRunSummary runOnce();

    List<AgentDecisionDto> getRecentDecisions(int limit);
}
