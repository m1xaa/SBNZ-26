package com.cr.back.player.dto;

import com.cr.back.domain.match.MatchEventType;

public record MatchEventRequest(
        MatchEventType type,
        int occurredAtSecond,
        double value
) {
}
