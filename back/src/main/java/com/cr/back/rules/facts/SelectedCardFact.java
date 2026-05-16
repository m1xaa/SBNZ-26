package com.cr.back.rules.facts;

import com.cr.back.domain.deck.Archetype;
import com.cr.back.domain.card.CardRole;

public record SelectedCardFact(
        Archetype archetype,
        CardRole role,
        CardFact card,
        String reason
) {
}
