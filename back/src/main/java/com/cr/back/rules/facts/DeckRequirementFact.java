package com.cr.back.rules.facts;

import com.cr.back.domain.deck.Archetype;
import com.cr.back.domain.card.CardRole;

public record DeckRequirementFact(
        Archetype archetype,
        CardRole role,
        int minimumCount,
        double maxElixir,
        String code,
        String reason
) {
}
