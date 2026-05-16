package com.cr.back.admin.dto;

import com.cr.back.domain.deck.Archetype;
import com.cr.back.domain.player.PlayerPlaystyle;

import java.util.Set;

public record PlayerProfileRequest(
        String username,
        PlayerPlaystyle playstyle,
        double maxPreferredAverageElixir,
        Archetype preferredArchetype,
        Set<String> dislikedCards
) {
    public PlayerProfileRequest {
        dislikedCards = dislikedCards == null ? Set.of() : dislikedCards;
    }
}
