package com.cr.back.admin.service;

import com.cr.back.domain.card.CardEntity;
import com.cr.back.domain.card.CardRole;
import com.cr.back.domain.card.CardSynergyEntity;
import com.cr.back.domain.card.CardType;
import com.cr.back.domain.card.SynergyType;
import com.cr.back.domain.deck.Archetype;
import com.cr.back.domain.deck.ArchetypeDefinitionEntity;
import com.cr.back.domain.deck.DeckValidationRuleEntity;
import com.cr.back.domain.match.MatchEventType;
import com.cr.back.domain.match.MatchOutcome;
import com.cr.back.domain.player.PlayerPlaystyle;
import com.cr.back.repository.ArchetypeDefinitionRepository;
import com.cr.back.repository.CardRepository;
import com.cr.back.repository.CardSynergyRepository;
import com.cr.back.repository.DeckValidationRuleRepository;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Service
public class StaticKnowledgeService {
    private final ArchetypeDefinitionRepository archetypeDefinitionRepository;
    private final DeckValidationRuleRepository validationRuleRepository;
    private final CardSynergyRepository synergyRepository;
    private final CardRepository cardRepository;

    public StaticKnowledgeService(
            ArchetypeDefinitionRepository archetypeDefinitionRepository,
            DeckValidationRuleRepository validationRuleRepository,
            CardSynergyRepository synergyRepository,
            CardRepository cardRepository
    ) {
        this.archetypeDefinitionRepository = archetypeDefinitionRepository;
        this.validationRuleRepository = validationRuleRepository;
        this.synergyRepository = synergyRepository;
        this.cardRepository = cardRepository;
    }

    public StaticKnowledgeSnapshot options() {
        return new StaticKnowledgeSnapshot(
                Arrays.asList(Archetype.values()),
                Arrays.asList(CardRole.values()),
                Arrays.asList(CardType.values()),
                Arrays.asList(MatchOutcome.values()),
                Arrays.asList(MatchEventType.values()),
                Arrays.asList(SynergyType.values()),
                Arrays.asList(PlayerPlaystyle.values())
        );
    }

    public List<PlayerPlaystyle> playerPlaystyles() {
        return Arrays.asList(PlayerPlaystyle.values());
    }

    public List<ArchetypeDefinitionEntity> archetypes() {
        return archetypeDefinitionRepository.findAll();
    }

    public ArchetypeDefinitionEntity createArchetype(
            Archetype archetype,
            String description,
            double minAverageElixir,
            double maxAverageElixir,
            Set<CardRole> requiredRoles
    ) {
        return archetypeDefinitionRepository.save(new ArchetypeDefinitionEntity(
                archetype,
                description,
                minAverageElixir,
                maxAverageElixir,
                requiredRoles
        ));
    }

    public ArchetypeDefinitionEntity updateArchetype(
            Long id,
            Archetype archetype,
            String description,
            double minAverageElixir,
            double maxAverageElixir,
            Set<CardRole> requiredRoles
    ) {
        ArchetypeDefinitionEntity entity = archetypeDefinitionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Archetype definition not found: " + id));
        entity.update(archetype, description, minAverageElixir, maxAverageElixir, requiredRoles);
        return archetypeDefinitionRepository.save(entity);
    }

    public List<DeckValidationRuleEntity> validationRules() {
        return validationRuleRepository.findAll();
    }

    public DeckValidationRuleEntity createValidationRule(String code, String description, Archetype archetype, boolean active) {
        return validationRuleRepository.save(new DeckValidationRuleEntity(code, description, archetype, active));
    }

    public DeckValidationRuleEntity updateValidationRule(Long id, String code, String description, Archetype archetype, boolean active) {
        DeckValidationRuleEntity entity = validationRuleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Validation rule not found: " + id));
        entity.update(code, description, archetype, active);
        return validationRuleRepository.save(entity);
    }

    public List<CardSynergyEntity> synergies() {
        return synergyRepository.findAll();
    }

    public CardSynergyEntity createSynergy(Long cardAId, Long cardBId, SynergyType type, int weight, String explanation) {
        return synergyRepository.save(new CardSynergyEntity(
                findCard(cardAId),
                findCard(cardBId),
                type,
                weight,
                explanation
        ));
    }

    public CardSynergyEntity updateSynergy(Long id, Long cardAId, Long cardBId, SynergyType type, int weight, String explanation) {
        CardSynergyEntity entity = synergyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Synergy not found: " + id));
        entity.update(findCard(cardAId), findCard(cardBId), type, weight, explanation);
        return synergyRepository.save(entity);
    }

    private CardEntity findCard(Long id) {
        return cardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Card not found: " + id));
    }

    public record StaticKnowledgeSnapshot(
            List<Archetype> archetypes,
            List<CardRole> cardRoles,
            List<CardType> cardTypes,
            List<MatchOutcome> matchOutcomes,
            List<MatchEventType> matchEventTypes,
            List<SynergyType> synergyTypes,
            List<PlayerPlaystyle> playerPlaystyles
    ) {
    }
}
