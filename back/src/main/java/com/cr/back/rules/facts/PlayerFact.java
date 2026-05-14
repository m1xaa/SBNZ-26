package com.cr.back.rules.facts;

import com.cr.back.domain.Archetype;

import java.util.Set;

public record PlayerFact(
        Long id,
        String username,
        boolean prefersFastGame,
        boolean likesHeavyDecks,
        boolean aggressivePressure,
        boolean patientGame,
        double maxPreferredAverageElixir,
        Archetype preferredArchetype,
        Set<String> dislikedCards
) {
}
