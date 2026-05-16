package com.cr.back.admin.dto;

import com.cr.back.domain.card.SynergyType;

public record SynergyRequest(
        Long cardAId,
        Long cardBId,
        SynergyType type,
        int weight,
        String explanation
) {
}
