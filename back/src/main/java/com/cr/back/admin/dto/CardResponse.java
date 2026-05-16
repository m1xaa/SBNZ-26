package com.cr.back.admin.dto;

import com.cr.back.card.CardImageSupport;
import com.cr.back.domain.card.CardEntity;
import com.cr.back.domain.card.CardRole;
import com.cr.back.domain.card.CardType;

import java.util.Set;

public record CardResponse(
        Long id,
        String name,
        Integer imageAssetId,
        String image,
        double elixirCost,
        CardType type,
        Set<CardRole> roles
) {
    public static CardResponse from(CardEntity card) {
        return new CardResponse(
                card.getId(),
                card.getName(),
                card.getImageAssetId(),
                CardImageSupport.loadBase64Image(card.getImageAssetId()),
                card.getElixirCost(),
                card.getType(),
                card.getRoles()
        );
    }
}
