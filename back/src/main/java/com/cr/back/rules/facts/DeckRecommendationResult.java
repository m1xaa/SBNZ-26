package com.cr.back.rules.facts;

public class DeckRecommendationResult {
    private DeckCandidate bestCandidate;

    public DeckCandidate getBestCandidate() {
        return bestCandidate;
    }

    public void select(DeckCandidate candidate) {
        this.bestCandidate = candidate;
    }
}
