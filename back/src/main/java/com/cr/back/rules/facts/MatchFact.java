package com.cr.back.rules.facts;

import com.cr.back.domain.Archetype;
import com.cr.back.domain.MatchOutcome;

public record MatchFact(
        Long id,
        MatchOutcome outcome,
        Archetype opponentArchetype,
        double deckAverageElixir,
        int durationSeconds
) {
}
