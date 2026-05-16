package com.cr.back.rules;

import com.cr.back.domain.card.CardRole;
import com.cr.back.domain.deck.Archetype;
import com.cr.back.rules.facts.CardFact;
import com.cr.back.rules.facts.CardPriorityFact;
import com.cr.back.rules.facts.DeckCandidate;
import com.cr.back.rules.facts.DeckRequirementFact;
import com.cr.back.rules.facts.PlayerFact;
import com.cr.back.rules.facts.SelectedCardFact;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public final class DeckRulesSupport {
    private DeckRulesSupport() {
    }

    public static boolean hasHighLevelRole(Collection<CardFact> cards, CardRole role, int minimumLevel) {
        return cards.stream().anyMatch(card -> card.unlocked() && card.level() >= minimumLevel && card.hasRole(role));
    }

    public static boolean hasUnlockedCard(Collection<CardFact> cards, String cardName, int minimumLevel) {
        return cards.stream().anyMatch(card -> card.unlocked() && card.level() >= minimumLevel && card.name().equals(cardName));
    }

    public static CardFact bestAvailableForRequirement(
            Collection<CardFact> cards,
            Collection<CardPriorityFact> priorities,
            DeckRequirementFact requirement,
            Collection<SelectedCardFact> alreadySelected
    ) {
        DeckCandidate selectedForArchetype = new DeckCandidate(requirement.archetype());
        alreadySelected.stream()
                .filter(selected -> selected.archetype() == requirement.archetype())
                .map(SelectedCardFact::card)
                .forEach(selectedForArchetype::addCard);
        return best(cards, priorities, requirement.archetype(), requirement.role(), requirement.maxElixir(), selectedForArchetype)
                .orElse(null);
    }

    public static boolean hasAvailableForRequirement(
            Collection<CardFact> cards,
            Collection<CardPriorityFact> priorities,
            DeckRequirementFact requirement,
            Collection<SelectedCardFact> alreadySelected
    ) {
        return bestAvailableForRequirement(cards, priorities, requirement, alreadySelected) != null;
    }

    public static CardFact bestAvailableForDeck(
            Collection<CardFact> cards,
            Collection<CardPriorityFact> priorities,
            Archetype archetype,
            CardRole role,
            double maxElixir,
            DeckCandidate candidate
    ) {
        return best(cards, priorities, archetype, role, maxElixir, candidate).orElse(null);
    }

    public static boolean hasAvailableForDeck(
            Collection<CardFact> cards,
            Collection<CardPriorityFact> priorities,
            Archetype archetype,
            CardRole role,
            double maxElixir,
            DeckCandidate candidate
    ) {
        return bestAvailableForDeck(cards, priorities, archetype, role, maxElixir, candidate) != null;
    }

    public static CardFact bestAlternative(
            Collection<CardFact> cards,
            Collection<CardPriorityFact> priorities,
            Archetype archetype,
            Set<CardRole> roles,
            DeckCandidate candidate,
            String excludedCardName
    ) {
        Set<String> selectedNames = candidate.getCards().stream()
                .map(CardFact::name)
                .filter(name -> !name.equals(excludedCardName))
                .collect(Collectors.toSet());
        return cards.stream()
                .filter(CardFact::unlocked)
                .filter(card -> !card.name().equals(excludedCardName))
                .filter(card -> !selectedNames.contains(card.name()))
                .filter(card -> roles.stream().anyMatch(card::hasRole))
                .max(Comparator
                        .comparingInt((CardFact card) -> replacementPriorityScore(priorities, archetype, roles, card.name()))
                        .thenComparingInt(CardFact::level)
                        .thenComparing(card -> -card.elixirCost()))
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

    public static boolean hasSelectedLowLevelCard(DeckCandidate candidate, int minimumLevel) {
        return candidate.getCards().stream().anyMatch(card -> card.level() < minimumLevel);
    }

    private static Optional<CardFact> best(
            Collection<CardFact> cards,
            Collection<CardPriorityFact> priorities,
            Archetype archetype,
            CardRole role,
            double maxElixir,
            DeckCandidate candidate
    ) {
        Set<String> selectedNames = candidate.getCards().stream().map(CardFact::name).collect(Collectors.toSet());
        return cards.stream()
                .filter(CardFact::unlocked)
                .filter(card -> card.hasRole(role))
                .filter(card -> card.elixirCost() <= maxElixir)
                .filter(card -> !selectedNames.contains(card.name()))
                .filter(card -> role == CardRole.WIN_CONDITION || candidate.countRole(CardRole.WIN_CONDITION) == 0 || !card.hasRole(CardRole.WIN_CONDITION))
                .max(candidateComparator(archetype, role, priorities));
    }

    private static Comparator<CardFact> candidateComparator(
            Archetype archetype,
            CardRole requestedRole,
            Collection<CardPriorityFact> priorities
    ) {
        return Comparator
                .comparingInt((CardFact card) -> priorityScore(priorities, archetype, requestedRole, card.name()))
                .thenComparingInt(CardFact::level)
                .thenComparing(card -> -card.elixirCost());
    }

    private static int priorityScore(
            Collection<CardPriorityFact> priorities,
            Archetype archetype,
            CardRole requestedRole,
            String cardName
    ) {
        return priorities.stream()
                .filter(priority -> priority.archetype() == archetype)
                .filter(priority -> priority.role() == requestedRole)
                .filter(priority -> priority.cardName().equals(cardName))
                .mapToInt(CardPriorityFact::score)
                .sum();
    }

    private static int replacementPriorityScore(
            Collection<CardPriorityFact> priorities,
            Archetype archetype,
            Set<CardRole> roles,
            String cardName
    ) {
        return priorities.stream()
                .filter(priority -> priority.archetype() == archetype)
                .filter(priority -> roles.contains(priority.role()))
                .filter(priority -> priority.cardName().equals(cardName))
                .mapToInt(CardPriorityFact::score)
                .sum();
    }
}
