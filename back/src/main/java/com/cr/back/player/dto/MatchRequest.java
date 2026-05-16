package com.cr.back.player.dto;

import com.cr.back.domain.deck.Archetype;
import com.cr.back.domain.match.MatchOutcome;

import java.time.Instant;
import java.util.List;

public record MatchRequest(
        MatchOutcome outcome,
        Archetype opponentArchetype,
        double deckAverageElixir,
        int durationSeconds,
        Instant playedAt,
        List<MatchEventRequest> events
) {
    public MatchRequest {
        events = events == null ? List.of() : events;
    }
}
