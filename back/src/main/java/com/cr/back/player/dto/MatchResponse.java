package com.cr.back.player.dto;

import com.cr.back.domain.deck.Archetype;
import com.cr.back.domain.match.MatchEntity;
import com.cr.back.domain.match.MatchEventEntity;
import com.cr.back.domain.match.MatchOutcome;

import java.time.Instant;
import java.util.List;

public record MatchResponse(
        Long id,
        MatchOutcome outcome,
        Archetype opponentArchetype,
        double deckAverageElixir,
        int durationSeconds,
        Instant playedAt,
        List<MatchEventResponse> events
) {
    public static MatchResponse from(MatchEntity match, List<MatchEventEntity> events) {
        return new MatchResponse(
                match.getId(),
                match.getOutcome(),
                match.getOpponentArchetype(),
                match.getDeckAverageElixir(),
                match.getDurationSeconds(),
                match.getPlayedAt(),
                events.stream().map(MatchEventResponse::from).toList()
        );
    }
}
