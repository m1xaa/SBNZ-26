package com.cr.back.player.dto;

import com.cr.back.domain.player.PlayerPlaystyle;

public record PlaystyleOptionResponse(
        PlayerPlaystyle playstyle,
        String displayName,
        String description,
        double defaultMaxPreferredAverageElixir
) {
    public static PlaystyleOptionResponse from(PlayerPlaystyle playstyle) {
        return new PlaystyleOptionResponse(
                playstyle,
                playstyle.getDisplayName(),
                playstyle.getDescription(),
                playstyle.getDefaultMaxPreferredAverageElixir()
        );
    }
}
