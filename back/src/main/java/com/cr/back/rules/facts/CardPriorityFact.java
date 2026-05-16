package com.cr.back.rules.facts;

import com.cr.back.domain.card.CardRole;
import com.cr.back.domain.deck.Archetype;

public record CardPriorityFact(
        Archetype archetype,
        CardRole role,
        String cardName,
        int score,
        String reason
) {
}
