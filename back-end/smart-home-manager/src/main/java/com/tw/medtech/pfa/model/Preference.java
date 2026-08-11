package com.tw.medtech.pfa.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "preferences")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Preference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Null = applies to every room this user is currently assigned to.
    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    // Free-form — this is the actual preference, in the user's own words.
    @Column(length = 2000, nullable = false)
    private String text;

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;

    @CreationTimestamp
    private Instant createdAt;
}
