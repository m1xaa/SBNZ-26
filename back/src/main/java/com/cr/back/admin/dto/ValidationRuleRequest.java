package com.cr.back.admin.dto;

import com.cr.back.domain.deck.Archetype;

public record ValidationRuleRequest(
        String code,
        String description,
        Archetype archetype,
        boolean active
) {
}
