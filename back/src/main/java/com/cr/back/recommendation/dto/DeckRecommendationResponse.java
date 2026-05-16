package com.cr.back.recommendation.dto;

import com.cr.back.domain.deck.Archetype;

import java.util.List;
import java.util.Map;

public record DeckRecommendationResponse(
        Long playerId,
        String username,
        Archetype archetype,
        int score,
        double averageElixir,
        List<RecommendedCardResponse> cards,
        List<String> insights,
        List<String> reasons,
        List<String> warnings,
        Map<String, String> alternatives
) {
}
