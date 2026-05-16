package com.cr.back.player.dto;

import com.cr.back.domain.match.MatchEventEntity;
import com.cr.back.domain.match.MatchEventType;

public record MatchEventResponse(
        MatchEventType type,
        int occurredAtSecond,
        double value
) {
    public static MatchEventResponse from(MatchEventEntity event) {
        return new MatchEventResponse(event.getType(), event.getOccurredAtSecond(), event.getValue());
    }
}
