package com.cr.back.domain.deck;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "deck_validation_rules")
public class DeckValidationRuleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;
    private String description;

    @Enumerated(EnumType.STRING)
    private Archetype archetype;

    private boolean active;

    protected DeckValidationRuleEntity() {
    }

    public DeckValidationRuleEntity(String code, String description, Archetype archetype, boolean active) {
        this.code = code;
        this.description = description;
        this.archetype = archetype;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public Archetype getArchetype() {
        return archetype;
    }

    public boolean isActive() {
        return active;
    }

    public void update(String code, String description, Archetype archetype, boolean active) {
        this.code = code;
        this.description = description;
        this.archetype = archetype;
        this.active = active;
    }
}
