package com.tw.medtech.pfa.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

// One row per (preference, room) evaluation the agent performs — whether
// or not it resulted in any device change. Deliberately NOT a relation to
// Preference/User/Room (no @ManyToOne) — this is an audit trail, and
// should survive the preference being edited or deleted later, or the
// room being renamed. Everything meaningful about the moment of the
// decision is snapshotted as plain columns instead.
@Entity
@Table(name = "agent_decisions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgentDecision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long preferenceId;

    @Column(length = 2000, nullable = false)
    private String preferenceText;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long roomId;

    @Column(nullable = false)
    private String roomName;

    // Raw JSON snapshot of the room's sensor readings at evaluation time —
    // what the LLM actually saw. Kept for backward compatibility with
    // existing rows; sensorDetailsJson (below) is the human-readable
    // successor, added alongside rather than replacing this, since
    // renaming a column under ddl-auto:update leaves the old one
    // orphaned rather than actually renaming it.
    @Column(length = 2000)
    private String sensorSnapshot;

    // The LLM's one-line explanation of its decision (or, for skipped/
    // failed evaluations, why nothing was sent to it at all).
    @Column(length = 1000)
    private String summary;

    // Raw JSON array of the actions actually applied — post-validation,
    // so this can be a subset of what the LLM proposed (invalid device
    // ids / statuses are dropped before this is written). Each entry now
    // includes deviceName/previousStatus alongside newStatus/reasoning —
    // see AgentAction.
    @Column(length = 2000)
    private String actionsJson;

    // Structured, human-readable sensor readings at evaluation time —
    // List<SensorDetail> serialized to JSON. The readable counterpart to
    // sensorSnapshot's raw form.
    @Column(length = 2000)
    private String sensorDetailsJson;

    // Denormalized from actionsJson, so cooldown lookups (see
    // AgentDecisionRepository) don't need to parse JSON to filter.
    @Column(nullable = false)
    private boolean hasActions;

    @CreationTimestamp
    private Instant createdAt;
}
