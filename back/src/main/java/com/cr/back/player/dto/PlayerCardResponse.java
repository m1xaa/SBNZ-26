package com.cr.back.player.dto;

import com.cr.back.domain.player.PlayerCardEntity;

public record PlayerCardResponse(
        Long cardId,
        String cardName,
        boolean unlocked,
        int level
) {
    public static PlayerCardResponse from(PlayerCardEntity entity) {
        return new PlayerCardResponse(
                entity.getCard().getId(),
                entity.getCard().getName(),
                entity.isUnlocked(),
                entity.getLevel()
        );
    }
}
