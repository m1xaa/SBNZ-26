package com.cr.back.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "card_synergies",
        uniqueConstraints = @UniqueConstraint(columnNames = {"card_a_id", "card_b_id"})
)
public class CardSynergyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "card_a_id")
    private CardEntity cardA;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "card_b_id")
    private CardEntity cardB;

    @Enumerated(EnumType.STRING)
    private SynergyType type;

    private int weight;
    private String explanation;

    protected CardSynergyEntity() {
    }

    public CardSynergyEntity(CardEntity cardA, CardEntity cardB, SynergyType type, int weight, String explanation) {
        this.cardA = cardA;
        this.cardB = cardB;
        this.type = type;
        this.weight = weight;
        this.explanation = explanation;
    }

    public Long getId() {
        return id;
    }

    public CardEntity getCardA() {
        return cardA;
    }

    public CardEntity getCardB() {
        return cardB;
    }

    public SynergyType getType() {
        return type;
    }

    public int getWeight() {
        return weight;
    }

    public String getExplanation() {
        return explanation;
    }

    public void update(CardEntity cardA, CardEntity cardB, SynergyType type, int weight, String explanation) {
        this.cardA = cardA;
        this.cardB = cardB;
        this.type = type;
        this.weight = weight;
        this.explanation = explanation;
    }
}
