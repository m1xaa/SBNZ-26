package com.cr.back.rules.facts;

import com.cr.back.domain.Archetype;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ArchetypeScore {
    private final Archetype archetype;
    private int score;
    private final List<String> reasons = new ArrayList<>();
    private final Set<String> appliedRules = new HashSet<>();

    public ArchetypeScore(Archetype archetype) {
        this.archetype = archetype;
    }

    public Archetype getArchetype() {
        return archetype;
    }

    public int getScore() {
        return score;
    }

    public void addScore(int value, String reason) {
        score += value;
        reasons.add(reason);
    }

    public List<String> getReasons() {
        return reasons;
    }

    public boolean hasAppliedRule(String rule) {
        return appliedRules.contains(rule);
    }

    public void markRule(String rule) {
        appliedRules.add(rule);
    }
}
