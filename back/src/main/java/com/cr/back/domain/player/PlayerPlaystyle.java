package com.cr.back.domain.player;

public enum PlayerPlaystyle {
    FAST_CYCLE("Fast Cycle", "Low-elixir cycle play with quick pressure and frequent card rotation.", 3.3),
    TACTICAL_CONTROL("Tactical Control", "Patient defensive play focused on positive trades and controlled counterpressure.", 3.8),
    AGGRESSIVE_BRIDGE_SPAM("Aggressive Bridge Spam", "Punish-oriented pressure that attacks elixir gaps quickly.", 3.6),
    PATIENT_BEATDOWN("Patient Beatdown", "Slower heavy-deck play that builds large pushes behind tanks.", 4.6),
    BALANCED("Balanced", "Flexible playstyle without a strong speed or deck-weight preference.", 4.0);

    private final String displayName;
    private final String description;
    private final double defaultMaxPreferredAverageElixir;

    PlayerPlaystyle(String displayName, String description, double defaultMaxPreferredAverageElixir) {
        this.displayName = displayName;
        this.description = description;
        this.defaultMaxPreferredAverageElixir = defaultMaxPreferredAverageElixir;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public double getDefaultMaxPreferredAverageElixir() {
        return defaultMaxPreferredAverageElixir;
    }

    public boolean prefersFastGame() {
        return this == FAST_CYCLE || this == AGGRESSIVE_BRIDGE_SPAM;
    }

    public boolean likesHeavyDecks() {
        return this == PATIENT_BEATDOWN;
    }

    public boolean aggressivePressure() {
        return this == FAST_CYCLE || this == AGGRESSIVE_BRIDGE_SPAM;
    }

    public boolean patientGame() {
        return this == TACTICAL_CONTROL || this == PATIENT_BEATDOWN;
    }
}
