package com.tw.medtech.pfa.dao.repository;

import com.tw.medtech.pfa.model.AgentDecision;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AgentDecisionRepository extends JpaRepository<AgentDecision, Long> {

    // Cooldown check: has this (preference, room) pair already resulted in
    // an actual device change recently? Deliberately scoped to the pair,
    // not per-device — simpler than parsing actionsJson to check a
    // specific device, and the pair is the right granularity for "don't
    // keep re-deciding the same thing every tick."
    Optional<AgentDecision> findFirstByPreferenceIdAndRoomIdAndHasActionsTrueOrderByCreatedAtDesc(
            Long preferenceId, Long roomId);

    List<AgentDecision> findTop50ByOrderByCreatedAtDesc();
}
