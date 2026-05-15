package com.cr.back.rules.facts;

import com.cr.back.domain.Archetype;
import com.cr.back.domain.CardRole;

public record SelectedCardFact(
        Archetype archetype,
        CardRole role,
        CardFact card,
        String reason
) {
}
