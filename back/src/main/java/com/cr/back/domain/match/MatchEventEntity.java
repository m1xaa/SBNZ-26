package com.cr.back.domain.match;

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

@Entity
@Table(name = "match_events")
public class MatchEventEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id")
    private MatchEntity match;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchEventType type;

    private int occurredAtSecond;
    private double value;

    protected MatchEventEntity() {
    }

    public MatchEventEntity(MatchEntity match, MatchEventType type, int occurredAtSecond, double value) {
        this.match = match;
        this.type = type;
        this.occurredAtSecond = occurredAtSecond;
        this.value = value;
    }

    public MatchEntity getMatch() {
        return match;
    }

    public MatchEventType getType() {
        return type;
    }

    public int getOccurredAtSecond() {
        return occurredAtSecond;
    }

    public double getValue() {
        return value;
    }
}
