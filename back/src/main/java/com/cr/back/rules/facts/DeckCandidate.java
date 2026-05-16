package com.cr.back.rules.facts;

import com.cr.back.domain.deck.Archetype;
import com.cr.back.domain.card.CardRole;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class DeckCandidate {
    private final Archetype archetype;
    private final List<CardFact> cards = new ArrayList<>();
    private final List<String> reasons = new ArrayList<>();
    private final List<String> warnings = new ArrayList<>();
    private final Map<String, String> alternatives = new LinkedHashMap<>();
    private final Set<String> appliedRules = new HashSet<>();
    private int score;
    private boolean valid = true;

    public DeckCandidate(Archetype archetype) {
        this.archetype = archetype;
    }

    public Archetype getArchetype() {
        return archetype;
    }

    public List<CardFact> getCards() {
        return cards;
    }

    public List<String> getReasons() {
        return reasons;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public Map<String, String> getAlternatives() {
        return alternatives;
    }

    public int getScore() {
        return score;
    }

    public void addScore(int value, String reason) {
        score += value;
        reasons.add(reason);
    }

    public boolean isValid() {
        return valid;
    }

    public void reject(String reason) {
        valid = false;
        warnings.add(reason);
    }

    public boolean contains(String cardName) {
        return cards.stream().anyMatch(card -> card.name().equals(cardName));
    }

    public boolean addCard(CardFact card) {
        if (card == null || contains(card.name()) || cards.size() >= 8) {
            return false;
        }
        cards.add(card);
        return true;
    }

    public int countRole(CardRole role) {
        return (int) cards.stream().filter(card -> card.hasRole(role)).count();
    }

    public boolean hasRole(CardRole role) {
        return countRole(role) > 0;
    }

    public double averageElixir() {
        if (cards.isEmpty()) {
            return 0;
        }
        return cards.stream().mapToDouble(CardFact::elixirCost).average().orElse(0);
    }

    public int keyLevelSum() {
        return cards.stream()
                .filter(card -> card.hasRole(CardRole.WIN_CONDITION) || card.hasRole(CardRole.SUPPORT))
                .mapToInt(CardFact::level)
                .sum();
    }

    public Optional<CardFact> lowestPrioritySupport() {
        return cards.stream()
                .filter(card -> card.hasRole(CardRole.SUPPORT) || card.hasRole(CardRole.CYCLE))
                .min((left, right) -> Integer.compare(left.level(), right.level()));
    }

    public boolean hasAppliedRule(String rule) {
        return appliedRules.contains(rule);
    }

    public void markRule(String rule) {
        appliedRules.add(rule);
    }

    public boolean betterThan(DeckCandidate other) {
        if (other == null) {
            return true;
        }
        if (valid != other.valid) {
            return valid;
        }
        if (score != other.score) {
            return score > other.score;
        }
        if (keyLevelSum() != other.keyLevelSum()) {
            return keyLevelSum() > other.keyLevelSum();
        }
        return averageElixir() < other.averageElixir();
    }
}
