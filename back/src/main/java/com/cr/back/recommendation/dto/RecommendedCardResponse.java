package com.cr.back.recommendation.dto;

public record RecommendedCardResponse(
        String name,
        int level,
        String image
) {
}
