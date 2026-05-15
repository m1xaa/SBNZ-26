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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "players")
public class PlayerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    private boolean prefersFastGame;
    private boolean likesHeavyDecks;
    private boolean aggressivePressure;
    private boolean patientGame;
    private double maxPreferredAverageElixir;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "preferred_playstyle_id")
    private PlayerPlaystyleEntity preferredPlaystyle;

    @Enumerated(EnumType.STRING)
    private Archetype preferredArchetype;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "player_disliked_cards", joinColumns = @JoinColumn(name = "player_id"))
    @Column(name = "card_name")
    private Set<String> dislikedCards = new HashSet<>();

    protected PlayerEntity() {
    }

    public PlayerEntity(
            String username,
            boolean prefersFastGame,
            boolean likesHeavyDecks,
            boolean aggressivePressure,
            boolean patientGame,
            double maxPreferredAverageElixir,
            PlayerPlaystyleEntity preferredPlaystyle,
            Archetype preferredArchetype
    ) {
        this.username = username;
        this.prefersFastGame = prefersFastGame;
        this.likesHeavyDecks = likesHeavyDecks;
        this.aggressivePressure = aggressivePressure;
        this.patientGame = patientGame;
        this.maxPreferredAverageElixir = maxPreferredAverageElixir;
        this.preferredPlaystyle = preferredPlaystyle;
        this.preferredArchetype = preferredArchetype;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public boolean isPrefersFastGame() {
        return prefersFastGame;
    }

    public boolean isLikesHeavyDecks() {
        return likesHeavyDecks;
    }

    public boolean isAggressivePressure() {
        return aggressivePressure;
    }

    public boolean isPatientGame() {
        return patientGame;
    }

    public double getMaxPreferredAverageElixir() {
        return maxPreferredAverageElixir;
    }

    public PlayerPlaystyleEntity getPreferredPlaystyle() {
        return preferredPlaystyle;
    }

    public Archetype getPreferredArchetype() {
        return preferredArchetype;
    }

    public Set<String> getDislikedCards() {
        return dislikedCards;
    }

    public void updateProfile(
            String username,
            boolean prefersFastGame,
            boolean likesHeavyDecks,
            boolean aggressivePressure,
            boolean patientGame,
            double maxPreferredAverageElixir,
            PlayerPlaystyleEntity preferredPlaystyle,
            Archetype preferredArchetype,
            Set<String> dislikedCards
    ) {
        this.username = username;
        this.prefersFastGame = prefersFastGame;
        this.likesHeavyDecks = likesHeavyDecks;
        this.aggressivePressure = aggressivePressure;
        this.patientGame = patientGame;
        this.maxPreferredAverageElixir = maxPreferredAverageElixir;
        this.preferredPlaystyle = preferredPlaystyle;
        this.preferredArchetype = preferredArchetype;
        this.dislikedCards = new HashSet<>(dislikedCards);
    }

    public void applyPlaystyle(PlayerPlaystyleEntity playstyle) {
        this.preferredPlaystyle = playstyle;
        if (playstyle == null) {
            return;
        }
        this.prefersFastGame = playstyle.isPrefersFastGame();
        this.likesHeavyDecks = playstyle.isLikesHeavyDecks();
        this.aggressivePressure = playstyle.isAggressivePressure();
        this.patientGame = playstyle.isPatientGame();
        this.maxPreferredAverageElixir = playstyle.getDefaultMaxPreferredAverageElixir();
    }
}
