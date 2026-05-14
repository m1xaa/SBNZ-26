package com.cr.back.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "match_history")
public class MatchEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id")
    private PlayerEntity player;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchOutcome outcome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Archetype opponentArchetype;

    private double deckAverageElixir;
    private int durationSeconds;
    private Instant playedAt;

    protected MatchEntity() {
    }

    public MatchEntity(
            PlayerEntity player,
            MatchOutcome outcome,
            Archetype opponentArchetype,
            double deckAverageElixir,
            int durationSeconds,
            Instant playedAt
    ) {
        this.player = player;
        this.outcome = outcome;
        this.opponentArchetype = opponentArchetype;
        this.deckAverageElixir = deckAverageElixir;
        this.durationSeconds = durationSeconds;
        this.playedAt = playedAt;
    }

    public Long getId() {
        return id;
    }

    public MatchOutcome getOutcome() {
        return outcome;
    }

    public Archetype getOpponentArchetype() {
        return opponentArchetype;
    }

    public double getDeckAverageElixir() {
        return deckAverageElixir;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public Instant getPlayedAt() {
        return playedAt;
    }
}
