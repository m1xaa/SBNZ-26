package com.cr.back.rules.facts;

import com.cr.back.domain.MatchEventType;

public record MatchEventFact(
        Long matchId,
        MatchEventType type,
        int occurredAtSecond
) {
}
