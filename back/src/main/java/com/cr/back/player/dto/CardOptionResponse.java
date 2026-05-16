package com.cr.back.player.dto;

import com.cr.back.card.CardImageSupport;
import com.cr.back.domain.card.CardEntity;

public record CardOptionResponse(
        Long id,
        String name,
        Integer imageAssetId,
        String image
) {
    public static CardOptionResponse from(CardEntity card) {
        return new CardOptionResponse(
                card.getId(),
                card.getName(),
                card.getImageAssetId(),
                CardImageSupport.loadBase64Image(card.getImageAssetId())
        );
    }
}
