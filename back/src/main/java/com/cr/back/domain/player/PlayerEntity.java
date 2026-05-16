package com.cr.back.domain.player;

import com.cr.back.domain.deck.Archetype;
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
@Table(name = "players")
public class PlayerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlayerPlaystyle playstyle;

    private double maxPreferredAverageElixir;

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
            PlayerPlaystyle playstyle,
            double maxPreferredAverageElixir,
            Archetype preferredArchetype
    ) {
        this.username = username;
        this.playstyle = playstyle;
        this.maxPreferredAverageElixir = maxPreferredAverageElixir;
        this.preferredArchetype = preferredArchetype;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public PlayerPlaystyle getPlaystyle() {
        return playstyle;
    }

    public double getMaxPreferredAverageElixir() {
        return maxPreferredAverageElixir;
    }

    public Archetype getPreferredArchetype() {
        return preferredArchetype;
    }

    public Set<String> getDislikedCards() {
        return dislikedCards;
    }

    public void updateProfile(
            String username,
            PlayerPlaystyle playstyle,
            double maxPreferredAverageElixir,
            Archetype preferredArchetype,
            Set<String> dislikedCards
    ) {
        this.username = username;
        this.playstyle = playstyle;
        this.maxPreferredAverageElixir = maxPreferredAverageElixir;
        this.preferredArchetype = preferredArchetype;
        this.dislikedCards = new HashSet<>(dislikedCards);
    }

    public void applyPlaystyle(PlayerPlaystyle playstyle) {
        this.playstyle = playstyle;
        if (playstyle != null) {
            this.maxPreferredAverageElixir = playstyle.getDefaultMaxPreferredAverageElixir();
        }
    }

    public boolean prefersFastGame() {
        return playstyle != null && playstyle.prefersFastGame();
    }

    public boolean likesHeavyDecks() {
        return playstyle != null && playstyle.likesHeavyDecks();
    }

    public boolean aggressivePressure() {
        return playstyle != null && playstyle.aggressivePressure();
    }

    public boolean patientGame() {
        return playstyle != null && playstyle.patientGame();
    }
}
