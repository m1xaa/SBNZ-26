package com.cr.back.admin.dto;

import com.cr.back.domain.card.CardRole;
import com.cr.back.domain.card.CardType;
import com.cr.back.domain.card.SynergyType;
import com.cr.back.domain.deck.Archetype;
import com.cr.back.domain.match.MatchEventType;
import com.cr.back.domain.match.MatchOutcome;
import com.cr.back.domain.player.PlayerPlaystyle;

import java.util.List;

public record StaticOptionsResponse(
        List<Archetype> archetypes,
        List<CardRole> cardRoles,
        List<CardType> cardTypes,
        List<MatchOutcome> matchOutcomes,
        List<MatchEventType> matchEventTypes,
        List<SynergyType> synergyTypes,
        List<PlayerPlaystyle> playerPlaystyles
) {
}
