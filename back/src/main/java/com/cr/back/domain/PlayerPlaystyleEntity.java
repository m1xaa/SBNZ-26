package com.cr.back.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "player_playstyles")
public class PlayerPlaystyleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 1000)
    private String description;

    private boolean prefersFastGame;
    private boolean likesHeavyDecks;
    private boolean aggressivePressure;
    private boolean patientGame;
    private double defaultMaxPreferredAverageElixir;

    protected PlayerPlaystyleEntity() {
    }

    public PlayerPlaystyleEntity(
            String code,
            String name,
            String description,
            boolean prefersFastGame,
            boolean likesHeavyDecks,
            boolean aggressivePressure,
            boolean patientGame,
            double defaultMaxPreferredAverageElixir
    ) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.prefersFastGame = prefersFastGame;
        this.likesHeavyDecks = likesHeavyDecks;
        this.aggressivePressure = aggressivePressure;
        this.patientGame = patientGame;
        this.defaultMaxPreferredAverageElixir = defaultMaxPreferredAverageElixir;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
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

    public double getDefaultMaxPreferredAverageElixir() {
        return defaultMaxPreferredAverageElixir;
    }
}
