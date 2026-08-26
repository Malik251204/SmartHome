package com.tw.medtech.pfa.web.controller;

import com.tw.medtech.pfa.service.AgentService;
import com.tw.medtech.pfa.web.dto.AgentDecisionDto;
import com.tw.medtech.pfa.web.dto.AgentRunSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/agent")
@RequiredArgsConstructor
public class AgentController {

    private final AgentService agentService;

    // Same evaluation logic the scheduled tick runs — for manual testing
    // (Postman) without waiting for the interval, and useful in a demo.
    @PostMapping("/run")
    public AgentRunSummary run() {
        return agentService.runOnce();
    }

    @GetMapping("/decisions")
    public List<AgentDecisionDto> recentDecisions(@RequestParam(defaultValue = "50") int limit) {
        return agentService.getRecentDecisions(limit);
    }
}
