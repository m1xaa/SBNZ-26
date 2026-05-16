package com.cr.back.admin.dto;

import com.cr.back.domain.deck.Archetype;
import com.cr.back.domain.player.PlayerEntity;
import com.cr.back.domain.player.PlayerPlaystyle;

import java.util.Set;

public record PlayerResponse(
        Long id,
        String username,
        PlayerPlaystyle playstyle,
        double maxPreferredAverageElixir,
        Archetype preferredArchetype,
        Set<String> dislikedCards
) {
    public static PlayerResponse from(PlayerEntity player) {
        return new PlayerResponse(
                player.getId(),
                player.getUsername(),
                player.getPlaystyle(),
                player.getMaxPreferredAverageElixir(),
                player.getPreferredArchetype(),
                player.getDislikedCards()
        );
    }
}
