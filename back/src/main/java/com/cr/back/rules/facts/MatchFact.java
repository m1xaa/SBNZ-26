package com.cr.back.rules.facts;

import com.cr.back.domain.deck.Archetype;
import com.cr.back.domain.match.MatchOutcome;
import org.kie.api.definition.type.Role;
import org.kie.api.definition.type.Timestamp;

@Role(Role.Type.EVENT)
@Timestamp("playedAtEpochMillis")
public class MatchFact {
    private final Long id;
    private final MatchOutcome outcome;
    private final Archetype opponentArchetype;
    private final double deckAverageElixir;
    private final int durationSeconds;
    private final long playedAtEpochMillis;

    public MatchFact(
            Long id,
            MatchOutcome outcome,
            Archetype opponentArchetype,
            double deckAverageElixir,
            int durationSeconds,
            long playedAtEpochMillis
    ) {
        this.id = id;
        this.outcome = outcome;
        this.opponentArchetype = opponentArchetype;
        this.deckAverageElixir = deckAverageElixir;
        this.durationSeconds = durationSeconds;
        this.playedAtEpochMillis = playedAtEpochMillis;
    }

    public Long getId() {
        return id;
    }

    public Long id() {
        return id;
    }

    public MatchOutcome getOutcome() {
        return outcome;
    }

    public MatchOutcome outcome() {
        return outcome;
    }

    public Archetype getOpponentArchetype() {
        return opponentArchetype;
    }

    public Archetype opponentArchetype() {
        return opponentArchetype;
    }

    public double getDeckAverageElixir() {
        return deckAverageElixir;
    }

    public double deckAverageElixir() {
        return deckAverageElixir;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public int durationSeconds() {
        return durationSeconds;
    }

    public long getPlayedAtEpochMillis() {
        return playedAtEpochMillis;
    }

    public long playedAtEpochMillis() {
        return playedAtEpochMillis;
    }
}
