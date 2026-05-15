package com.cr.back.rules.facts;

import com.cr.back.domain.Archetype;
import com.cr.back.domain.CardRole;

public record DeckRequirementFact(
        Archetype archetype,
        CardRole role,
        int minimumCount,
        double maxElixir,
        String code,
        String reason
) {
}
