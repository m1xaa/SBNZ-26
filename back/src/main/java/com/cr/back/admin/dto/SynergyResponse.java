package com.cr.back.admin.dto;

import com.cr.back.domain.card.CardSynergyEntity;
import com.cr.back.domain.card.SynergyType;

public record SynergyResponse(
        Long id,
        Long cardAId,
        String cardAName,
        Long cardBId,
        String cardBName,
        SynergyType type,
        int weight,
        String explanation
) {
    public static SynergyResponse from(CardSynergyEntity entity) {
        return new SynergyResponse(
                entity.getId(),
                entity.getCardA().getId(),
                entity.getCardA().getName(),
                entity.getCardB().getId(),
                entity.getCardB().getName(),
                entity.getType(),
                entity.getWeight(),
                entity.getExplanation()
        );
    }
}
