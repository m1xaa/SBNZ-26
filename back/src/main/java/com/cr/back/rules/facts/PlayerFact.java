package com.cr.back.rules.facts;

import com.cr.back.domain.deck.Archetype;
import com.cr.back.domain.player.PlayerPlaystyle;

import java.util.Set;

public record PlayerFact(
        Long id,
        String username,
        PlayerPlaystyle playstyle,
        double maxPreferredAverageElixir,
        Archetype preferredArchetype,
        Set<String> dislikedCards
) {
    public boolean prefersFastGame() {
        return playstyle != null && playstyle.prefersFastGame();
    }

    public boolean likesHeavyDecks() {
        return playstyle != null && playstyle.likesHeavyDecks();
    }

    public boolean aggressivePressure() {
        return playstyle != null && playstyle.aggressivePressure();
    }

    public boolean patientGame() {
        return playstyle != null && playstyle.patientGame();
    }
}
