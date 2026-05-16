package com.cr.back.admin.dto;

import com.cr.back.domain.card.CardRole;
import com.cr.back.domain.card.CardType;

import java.util.Set;

public record CardRequest(
        String name,
        String imageBase64,
        double elixirCost,
        CardType type,
        Set<CardRole> roles
) {
    public CardRequest {
        roles = roles == null ? Set.of() : roles;
    }
}
