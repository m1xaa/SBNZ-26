package com.cr.back.rules.facts;

import com.cr.back.domain.card.CardRole;
import com.cr.back.domain.card.CardType;

import java.util.Set;

public record CardFact(
        String name,
        Integer imageAssetId,
        double elixirCost,
        CardType type,
        Set<CardRole> roles,
        boolean unlocked,
        int level
) {
    public boolean hasRole(CardRole role) {
        return roles.contains(role);
    }
}
