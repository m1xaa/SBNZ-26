package com.cr.back.admin;

import com.cr.back.domain.Archetype;
import com.cr.back.domain.ArchetypeDefinitionEntity;
import com.cr.back.domain.CardEntity;
import com.cr.back.domain.CardRole;
import com.cr.back.domain.CardSynergyEntity;
import com.cr.back.domain.CardType;
import com.cr.back.domain.DeckValidationRuleEntity;
import com.cr.back.domain.MatchEventType;
import com.cr.back.domain.MatchOutcome;
import com.cr.back.domain.SynergyType;
import com.cr.back.repository.ArchetypeDefinitionRepository;
import com.cr.back.repository.CardRepository;
import com.cr.back.repository.CardSynergyRepository;
import com.cr.back.repository.DeckValidationRuleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/static")
public class StaticKnowledgeController {
    private final ArchetypeDefinitionRepository archetypeDefinitionRepository;
    private final DeckValidationRuleRepository validationRuleRepository;
    private final CardSynergyRepository synergyRepository;
    private final CardRepository cardRepository;

    public StaticKnowledgeController(
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

    @GetMapping("/options")
    public StaticOptions options() {
        return new StaticOptions(
                Arrays.asList(Archetype.values()),
                Arrays.asList(CardRole.values()),
                Arrays.asList(CardType.values()),
                Arrays.asList(MatchOutcome.values()),
                Arrays.asList(MatchEventType.values()),
                Arrays.asList(SynergyType.values())
        );
    }

    @GetMapping("/archetypes")
    public List<ArchetypeDefinitionEntity> archetypes() {
        return archetypeDefinitionRepository.findAll();
    }

    @PostMapping("/archetypes")
    @ResponseStatus(HttpStatus.CREATED)
    public ArchetypeDefinitionEntity createArchetype(@RequestBody ArchetypeDefinitionRequest request) {
        return archetypeDefinitionRepository.save(new ArchetypeDefinitionEntity(
                request.archetype(),
                request.description(),
                request.minAverageElixir(),
                request.maxAverageElixir(),
                request.requiredRoles()
        ));
    }

    @PutMapping("/archetypes/{id}")
    public ArchetypeDefinitionEntity updateArchetype(@PathVariable Long id, @RequestBody ArchetypeDefinitionRequest request) {
        ArchetypeDefinitionEntity entity = archetypeDefinitionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Archetype definition not found: " + id));
        entity.update(
                request.archetype(),
                request.description(),
                request.minAverageElixir(),
                request.maxAverageElixir(),
                request.requiredRoles()
        );
        return archetypeDefinitionRepository.save(entity);
    }

    @DeleteMapping("/archetypes/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteArchetype(@PathVariable Long id) {
        archetypeDefinitionRepository.deleteById(id);
    }

    @GetMapping("/validation-rules")
    public List<DeckValidationRuleEntity> validationRules() {
        return validationRuleRepository.findAll();
    }

    @PostMapping("/validation-rules")
    @ResponseStatus(HttpStatus.CREATED)
    public DeckValidationRuleEntity createValidationRule(@RequestBody ValidationRuleRequest request) {
        return validationRuleRepository.save(new DeckValidationRuleEntity(
                request.code(),
                request.description(),
                request.archetype(),
                request.active()
        ));
    }

    @PutMapping("/validation-rules/{id}")
    public DeckValidationRuleEntity updateValidationRule(@PathVariable Long id, @RequestBody ValidationRuleRequest request) {
        DeckValidationRuleEntity entity = validationRuleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Validation rule not found: " + id));
        entity.update(request.code(), request.description(), request.archetype(), request.active());
        return validationRuleRepository.save(entity);
    }

    @DeleteMapping("/validation-rules/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteValidationRule(@PathVariable Long id) {
        validationRuleRepository.deleteById(id);
    }

    @GetMapping("/synergies")
    public List<SynergyResponse> synergies() {
        return synergyRepository.findAll().stream().map(SynergyResponse::from).toList();
    }

    @PostMapping("/synergies")
    @ResponseStatus(HttpStatus.CREATED)
    public SynergyResponse createSynergy(@RequestBody SynergyRequest request) {
        CardEntity cardA = findCard(request.cardAId());
        CardEntity cardB = findCard(request.cardBId());
        return SynergyResponse.from(synergyRepository.save(new CardSynergyEntity(
                cardA,
                cardB,
                request.type(),
                request.weight(),
                request.explanation()
        )));
    }

    @PutMapping("/synergies/{id}")
    public SynergyResponse updateSynergy(@PathVariable Long id, @RequestBody SynergyRequest request) {
        CardSynergyEntity entity = synergyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Synergy not found: " + id));
        entity.update(findCard(request.cardAId()), findCard(request.cardBId()), request.type(), request.weight(), request.explanation());
        return SynergyResponse.from(synergyRepository.save(entity));
    }

    @DeleteMapping("/synergies/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSynergy(@PathVariable Long id) {
        synergyRepository.deleteById(id);
    }

    private CardEntity findCard(Long id) {
        return cardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Card not found: " + id));
    }

    public record StaticOptions(
            List<Archetype> archetypes,
            List<CardRole> cardRoles,
            List<CardType> cardTypes,
            List<MatchOutcome> matchOutcomes,
            List<MatchEventType> matchEventTypes,
            List<SynergyType> synergyTypes
    ) {
    }

    public record ArchetypeDefinitionRequest(
            Archetype archetype,
            String description,
            double minAverageElixir,
            double maxAverageElixir,
            Set<CardRole> requiredRoles
    ) {
        public ArchetypeDefinitionRequest {
            requiredRoles = requiredRoles == null ? Set.of() : requiredRoles;
        }
    }

    public record ValidationRuleRequest(String code, String description, Archetype archetype, boolean active) {
    }

    public record SynergyRequest(Long cardAId, Long cardBId, SynergyType type, int weight, String explanation) {
    }

    public record SynergyResponse(
            Long id,
            Long cardAId,
            String cardAName,
            Long cardBId,
            String cardBName,
            SynergyType type,
            int weight,
            String explanation
    ) {
        static SynergyResponse from(CardSynergyEntity entity) {
            return new SynergyResponse(
                    entity.getId(),
                    entity.getCardA().getId(),
                    entity.getCardA().getName(),
                    entity.getCardB().getId(),
                    entity.getCardB().getName(),
                    entity.getType(),
                    entity.getWeight(),
                    entity.getExplanation()
            );
        }
    }
}
