package com.cr.back.recommendation;

import com.cr.back.recommendation.dto.DeckRecommendationResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {
    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping("/{playerId}")
    public DeckRecommendationResponse recommend(@PathVariable Long playerId) {
        return recommendationService.recommend(playerId);
    }
}
