package com.cr.back.admin;

import com.cr.back.admin.dto.ArchetypeDefinitionRequest;
import com.cr.back.admin.dto.StaticOptionsResponse;
import com.cr.back.admin.dto.SynergyRequest;
import com.cr.back.admin.dto.SynergyResponse;
import com.cr.back.admin.dto.ValidationRuleRequest;
import com.cr.back.admin.service.StaticKnowledgeService;
import com.cr.back.admin.service.StaticKnowledgeService.StaticKnowledgeSnapshot;
import com.cr.back.domain.deck.ArchetypeDefinitionEntity;
import com.cr.back.domain.deck.DeckValidationRuleEntity;
import com.cr.back.domain.player.PlayerPlaystyle;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/static")
public class StaticKnowledgeController {
    private final StaticKnowledgeService staticKnowledgeService;

    public StaticKnowledgeController(StaticKnowledgeService staticKnowledgeService) {
        this.staticKnowledgeService = staticKnowledgeService;
    }

    @GetMapping("/options")
    public StaticOptionsResponse options() {
        StaticKnowledgeSnapshot snapshot = staticKnowledgeService.options();
        return new StaticOptionsResponse(
                snapshot.archetypes(),
                snapshot.cardRoles(),
                snapshot.cardTypes(),
                snapshot.matchOutcomes(),
                snapshot.matchEventTypes(),
                snapshot.synergyTypes(),
                snapshot.playerPlaystyles()
        );
    }

    @GetMapping("/player-playstyles")
    public List<PlayerPlaystyle> playerPlaystyles() {
        return staticKnowledgeService.playerPlaystyles();
    }

    @GetMapping("/archetypes")
    public List<ArchetypeDefinitionEntity> archetypes() {
        return staticKnowledgeService.archetypes();
    }

    @PostMapping("/archetypes")
    @ResponseStatus(HttpStatus.CREATED)
    public ArchetypeDefinitionEntity createArchetype(@RequestBody ArchetypeDefinitionRequest request) {
        return staticKnowledgeService.createArchetype(
                request.archetype(),
                request.description(),
                request.minAverageElixir(),
                request.maxAverageElixir(),
                request.requiredRoles()
        );
    }

    @PutMapping("/archetypes/{id}")
    public ArchetypeDefinitionEntity updateArchetype(@PathVariable Long id, @RequestBody ArchetypeDefinitionRequest request) {
        return staticKnowledgeService.updateArchetype(
                id,
                request.archetype(),
                request.description(),
                request.minAverageElixir(),
                request.maxAverageElixir(),
                request.requiredRoles()
        );
    }

    @GetMapping("/validation-rules")
    public List<DeckValidationRuleEntity> validationRules() {
        return staticKnowledgeService.validationRules();
    }

    @PostMapping("/validation-rules")
    @ResponseStatus(HttpStatus.CREATED)
    public DeckValidationRuleEntity createValidationRule(@RequestBody ValidationRuleRequest request) {
        return staticKnowledgeService.createValidationRule(
                request.code(),
                request.description(),
                request.archetype(),
                request.active()
        );
    }

    @PutMapping("/validation-rules/{id}")
    public DeckValidationRuleEntity updateValidationRule(@PathVariable Long id, @RequestBody ValidationRuleRequest request) {
        return staticKnowledgeService.updateValidationRule(
                id,
                request.code(),
                request.description(),
                request.archetype(),
                request.active()
        );
    }

    @GetMapping("/synergies")
    public List<SynergyResponse> synergies() {
        return staticKnowledgeService.synergies().stream().map(SynergyResponse::from).toList();
    }

    @PostMapping("/synergies")
    @ResponseStatus(HttpStatus.CREATED)
    public SynergyResponse createSynergy(@RequestBody SynergyRequest request) {
        return SynergyResponse.from(staticKnowledgeService.createSynergy(
                request.cardAId(),
                request.cardBId(),
                request.type(),
                request.weight(),
                request.explanation()
        ));
    }

    @PutMapping("/synergies/{id}")
    public SynergyResponse updateSynergy(@PathVariable Long id, @RequestBody SynergyRequest request) {
        return SynergyResponse.from(staticKnowledgeService.updateSynergy(
                id,
                request.cardAId(),
                request.cardBId(),
                request.type(),
                request.weight(),
                request.explanation()
        ));
    }
}
