package com.cr.back.domain.card;
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
@Table(name = "cards")
public class CardEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(unique = true)
    private Integer imageAssetId;

    @Column(nullable = false)
    private double elixirCost;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardType type;

    @ElementCollection(targetClass = CardRole.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "card_roles", joinColumns = @JoinColumn(name = "card_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Set<CardRole> roles = new HashSet<>();

    protected CardEntity() {
    }

    public CardEntity(String name, double elixirCost, CardType type, Set<CardRole> roles) {
        this(name, null, elixirCost, type, roles);
    }

    public CardEntity(String name, Integer imageAssetId, double elixirCost, CardType type, Set<CardRole> roles) {
        this.name = name;
        this.imageAssetId = imageAssetId;
        this.elixirCost = elixirCost;
        this.type = type;
        this.roles = new HashSet<>(roles);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Integer getImageAssetId() {
        return imageAssetId;
    }

    public double getElixirCost() {
        return elixirCost;
    }

    public CardType getType() {
        return type;
    }

    public Set<CardRole> getRoles() {
        return roles;
    }

    public void update(String name, double elixirCost, CardType type, Set<CardRole> roles) {
        update(name, this.imageAssetId, elixirCost, type, roles);
    }

    public void update(String name, Integer imageAssetId, double elixirCost, CardType type, Set<CardRole> roles) {
        this.name = name;
        this.imageAssetId = imageAssetId;
        this.elixirCost = elixirCost;
        this.type = type;
        this.roles = new HashSet<>(roles);
    }
}
