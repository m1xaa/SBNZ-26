package com.cr.back.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "archetype_definitions")
public class ArchetypeDefinitionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private Archetype archetype;

    private String description;
    private double minAverageElixir;
    private double maxAverageElixir;

    @ElementCollection(targetClass = CardRole.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "archetype_required_roles", joinColumns = @JoinColumn(name = "archetype_definition_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Set<CardRole> requiredRoles = new HashSet<>();

    protected ArchetypeDefinitionEntity() {
    }

    public ArchetypeDefinitionEntity(
            Archetype archetype,
            String description,
            double minAverageElixir,
            double maxAverageElixir,
            Set<CardRole> requiredRoles
    ) {
        this.archetype = archetype;
        this.description = description;
        this.minAverageElixir = minAverageElixir;
        this.maxAverageElixir = maxAverageElixir;
        this.requiredRoles = new HashSet<>(requiredRoles);
    }

    public Long getId() {
        return id;
    }

    public Archetype getArchetype() {
        return archetype;
    }

    public String getDescription() {
        return description;
    }

    public double getMinAverageElixir() {
        return minAverageElixir;
    }

    public double getMaxAverageElixir() {
        return maxAverageElixir;
    }

    public Set<CardRole> getRequiredRoles() {
        return requiredRoles;
    }

    public void update(
            Archetype archetype,
            String description,
            double minAverageElixir,
            double maxAverageElixir,
            Set<CardRole> requiredRoles
    ) {
        this.archetype = archetype;
        this.description = description;
        this.minAverageElixir = minAverageElixir;
        this.maxAverageElixir = maxAverageElixir;
        this.requiredRoles = new HashSet<>(requiredRoles);
    }
}
