package com.tw.medtech.pfa.scheduler;

import com.tw.medtech.pfa.service.AgentService;
import com.tw.medtech.pfa.web.dto.AgentRunSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

// Interval is configurable (agent.interval-ms) specifically so it can be
// set short for local testing (e.g. every few minutes) versus something
// closer to real usage (e.g. hourly) without touching code — same
// pattern as jwt.expiration-ms.
@Component
@RequiredArgsConstructor
@Slf4j
public class AgentScheduler {

    private final AgentService agentService;

    @Scheduled(fixedRateString = "${agent.interval-ms:3600000}")
    public void tick() {
        try {
            AgentRunSummary summary = agentService.runOnce();
            log.info("Agent run complete: evaluated {} preference/room pair(s), applied {} action(s).",
                    summary.evaluatedCount(), summary.actionsAppliedCount());
        } catch (Exception e) {
            // A failed run shouldn't stop future scheduled runs.
            log.error("Scheduled agent run failed", e);
        }
    }
}
