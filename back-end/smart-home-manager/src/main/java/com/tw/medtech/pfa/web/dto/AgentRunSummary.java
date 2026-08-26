package com.tw.medtech.pfa.web.dto;

import java.util.List;

public record AgentRunSummary(
        int evaluatedCount,
        int actionsAppliedCount,
        List<AgentDecisionDto> decisions
) {}
