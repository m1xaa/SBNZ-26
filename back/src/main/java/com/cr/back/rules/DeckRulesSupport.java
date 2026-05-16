package com.cr.back.rules;

import com.cr.back.domain.card.CardRole;
import com.cr.back.domain.deck.Archetype;
import com.cr.back.rules.facts.CardFact;
import com.cr.back.rules.facts.DeckCandidate;
import com.cr.back.rules.facts.DeckRequirementFact;
import com.cr.back.rules.facts.PlayerFact;
import com.cr.back.rules.facts.SelectedCardFact;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class DeckRulesSupport {
    private DeckRulesSupport() {
    }

    public static boolean hasHighLevelRole(Collection<CardFact> cards, CardRole role, int minimumLevel) {
        return cards.stream().anyMatch(card -> card.unlocked() && card.level() >= minimumLevel && card.hasRole(role));
    }

    public static boolean hasUnlockedCard(Collection<CardFact> cards, String cardName, int minimumLevel) {
        return cards.stream().anyMatch(card -> card.unlocked() && card.level() >= minimumLevel && card.name().equals(cardName));
    }

    public static boolean hasMultipleSpellBaitCards(Collection<CardFact> cards) {
        long baitCards = cards.stream()
                .filter(CardFact::unlocked)
                .filter(card -> card.hasRole(CardRole.SWARM) || card.name().equals("Goblin Barrel") || card.name().equals("Princess"))
                .count();
        return baitCards >= 3;
    }

    public static boolean hasBridgePressure(Collection<CardFact> cards) {
        return cards.stream()
                .filter(CardFact::unlocked)
                .filter(card -> card.hasRole(CardRole.PRESSURE))
                .count() >= 2;
    }

    public static void buildDeck(DeckCandidate candidate, Collection<CardFact> cards) {
        switch (candidate.getArchetype()) {
            case CYCLE, CONTROL, AIR_COUNTER -> addBest(candidate, cards, CardRole.WIN_CONDITION, 4.0);
            case BEATDOWN -> {
                addBestNamedOrRole(candidate, cards, "Golem", CardRole.TANK);
                addBest(candidate, cards, CardRole.SUPPORT, 5.0);
            }
            case BAIT -> addBestNamedOrRole(candidate, cards, "Goblin Barrel", CardRole.WIN_CONDITION);
            case BRIDGE_SPAM -> addBestNamedOrRole(candidate, cards, "Battle Ram", CardRole.PRESSURE);
            case SIEGE -> addBest(candidate, cards, CardRole.SIEGE, 6.0);
        }

        addBest(candidate, cards, CardRole.ANTI_AIR, 5.0);
        addBest(candidate, cards, CardRole.BUILDING, 5.0);
        addBest(candidate, cards, CardRole.SMALL_SPELL, 3.0);
        addBest(candidate, cards, CardRole.BIG_SPELL, 5.0);
        addBest(candidate, cards, CardRole.SINGLE_TARGET_DPS, 5.0);
        fill(candidate, cards, CardRole.CYCLE);
        fill(candidate, cards, CardRole.SUPPORT);
    }

    public static CardFact bestAvailableForRequirement(
            Collection<CardFact> cards,
            DeckRequirementFact requirement,
            Collection<SelectedCardFact> alreadySelected
    ) {
        DeckCandidate selectedForArchetype = new DeckCandidate(requirement.archetype());
        alreadySelected.stream()
                .filter(selected -> selected.archetype() == requirement.archetype())
                .map(SelectedCardFact::card)
                .forEach(selectedForArchetype::addCard);
        return best(cards, requirement.archetype(), requirement.role(), requirement.maxElixir(), selectedForArchetype)
                .orElse(null);
    }

    public static void addSelectedCards(DeckCandidate candidate, Collection<SelectedCardFact> selectedCards) {
        selectedCards.stream()
                .filter(selected -> selected.archetype() == candidate.getArchetype())
                .forEach(selected -> {
                    if (candidate.addCard(selected.card())) {
                        candidate.addScore(2, selected.reason());
                    }
                });
    }

    public static void addExtraAntiAir(DeckCandidate candidate, Collection<CardFact> cards) {
        while (candidate.countRole(CardRole.ANTI_AIR) < 2) {
            Optional<CardFact> antiAir = best(cards, candidate.getArchetype(), CardRole.ANTI_AIR, 5.0, candidate);
            if (antiAir.isEmpty()) {
                return;
            }
            Optional<CardFact> replace = candidate.lowestPrioritySupport();
            replace.ifPresent(card -> candidate.getCards().remove(card));
            candidate.addCard(antiAir.get());
        }
    }

    public static void replaceDislikedCards(DeckCandidate candidate, Collection<CardFact> cards, PlayerFact player) {
        List<CardFact> disliked = candidate.getCards().stream()
                .filter(card -> player.dislikedCards().contains(card.name()))
                .toList();
        for (CardFact card : disliked) {
            Optional<CardFact> replacement = card.roles().stream()
                    .map(role -> best(cards, candidate.getArchetype(), role, 6.0, candidate))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .findFirst();
            replacement.ifPresent(value -> {
                candidate.getCards().remove(card);
                candidate.addCard(value);
                candidate.getAlternatives().put(card.name(), value.name());
            });
        }
    }

    public static void suggestAlternativesForLowCards(DeckCandidate candidate, Collection<CardFact> cards) {
        candidate.getCards().stream()
                .filter(card -> card.level() < 10)
                .forEach(card -> card.roles().stream()
                        .map(role -> best(cards, candidate.getArchetype(), role, 10.0, candidate))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .findFirst()
                        .ifPresent(alternative -> candidate.getAlternatives().put(card.name(), alternative.name())));
    }

    public static boolean hasSelectedLowLevelCard(DeckCandidate candidate, int minimumLevel) {
        return candidate.getCards().stream().anyMatch(card -> card.level() < minimumLevel);
    }

    private static void addBest(DeckCandidate candidate, Collection<CardFact> cards, CardRole role, double maxElixir) {
        best(cards, candidate.getArchetype(), role, maxElixir, candidate).ifPresent(candidate::addCard);
    }

    private static void addBestNamedOrRole(DeckCandidate candidate, Collection<CardFact> cards, String name, CardRole role) {
        cards.stream()
                .filter(CardFact::unlocked)
                .filter(card -> card.name().equals(name))
                .max(Comparator.comparingInt(CardFact::level))
                .ifPresentOrElse(candidate::addCard, () -> addBest(candidate, cards, role, 9.0));
    }

    private static void fill(DeckCandidate candidate, Collection<CardFact> cards, CardRole role) {
        while (candidate.getCards().size() < 8) {
            Optional<CardFact> card = best(cards, candidate.getArchetype(), role, 9.0, candidate);
            if (card.isEmpty()) {
                return;
            }
            candidate.addCard(card.get());
        }
    }

    private static Optional<CardFact> best(
            Collection<CardFact> cards,
            Archetype archetype,
            CardRole role,
            double maxElixir,
            DeckCandidate candidate
    ) {
        return cards.stream()
                .filter(CardFact::unlocked)
                .filter(card -> card.hasRole(role))
                .filter(card -> card.elixirCost() <= maxElixir)
                .filter(card -> !candidate.contains(card.name()))
                .filter(card -> isAllowedForSlot(card, archetype, role, candidate))
                .max(candidateComparator(archetype, role));
    }

    private static boolean isAllowedForSlot(CardFact card, Archetype archetype, CardRole requestedRole, DeckCandidate candidate) {
        if (requestedRole != CardRole.WIN_CONDITION && candidate.countRole(CardRole.WIN_CONDITION) >= 1 && card.hasRole(CardRole.WIN_CONDITION)) {
            return false;
        }

        if (requestedRole == CardRole.WIN_CONDITION && archetype == Archetype.CYCLE) {
            return card.hasRole(CardRole.PRESSURE) || card.hasRole(CardRole.CYCLE);
        }

        if ((requestedRole == CardRole.CYCLE || requestedRole == CardRole.SUPPORT) && archetype == Archetype.CYCLE) {
            return !card.hasRole(CardRole.TANK);
        }

        return true;
    }

    private static Comparator<CardFact> candidateComparator(Archetype archetype, CardRole requestedRole) {
        return Comparator
                .comparingInt((CardFact card) -> archetypeFitScore(card, archetype, requestedRole))
                .thenComparingInt(CardFact::level)
                .thenComparing(card -> -card.elixirCost());
    }

    private static int archetypeFitScore(CardFact card, Archetype archetype, CardRole requestedRole) {
        int score = 0;

        if (requestedRole == CardRole.WIN_CONDITION) {
            score += 40;
        }

        if (archetype == Archetype.CYCLE) {
            if (card.hasRole(CardRole.CYCLE)) {
                score += 20;
            }
            if (card.hasRole(CardRole.PRESSURE)) {
                score += 15;
            }
            if (card.hasRole(CardRole.TANK)) {
                score -= 25;
            }
            if (card.elixirCost() <= 4.0) {
                score += 10;
            }
        }

        if (archetype == Archetype.BEATDOWN && card.hasRole(CardRole.TANK)) {
            score += 20;
        }

        if (requestedRole == CardRole.CYCLE && card.elixirCost() <= 2.0) {
            score += 8;
        }

        return score;
    }
}
