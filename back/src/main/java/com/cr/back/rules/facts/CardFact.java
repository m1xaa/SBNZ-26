package com.cr.back.rules.facts;

import com.cr.back.domain.CardRole;
import com.cr.back.domain.CardType;

import java.util.Set;

public record CardFact(
        String name,
        double elixirCost,
        CardType type,
        Set<CardRole> roles,
        boolean unlocked,
        int level,
        boolean reliablyUsed
) {
    public boolean hasRole(CardRole role) {
        return roles.contains(role);
    }
}
