package com.cr.back.admin.dto;

import com.cr.back.domain.card.CardRole;
import com.cr.back.domain.deck.Archetype;

import java.util.Set;

public record ArchetypeDefinitionRequest(
        Archetype archetype,
        String description,
        double minAverageElixir,
        double maxAverageElixir,
        Set<CardRole> requiredRoles
) {
    public ArchetypeDefinitionRequest {
        requiredRoles = requiredRoles == null ? Set.of() : requiredRoles;
    }
}
