package com.cr.back.player.dto;

public record PlayerCardRequest(
        Long cardId,
        boolean unlocked,
        int level
) {
}
