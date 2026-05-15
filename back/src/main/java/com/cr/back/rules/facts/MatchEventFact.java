package com.cr.back.rules.facts;

import com.cr.back.domain.MatchEventType;
import org.kie.api.definition.type.Role;
import org.kie.api.definition.type.Timestamp;

@Role(Role.Type.EVENT)
@Timestamp("eventTimestamp")
public class MatchEventFact {
    private final Long matchId;
    private final MatchEventType type;
    private final int occurredAtSecond;
    private final double value;
    private final long eventTimestamp;

    public MatchEventFact(Long matchId, MatchEventType type, int occurredAtSecond, double value, long eventTimestamp) {
        this.matchId = matchId;
        this.type = type;
        this.occurredAtSecond = occurredAtSecond;
        this.value = value;
        this.eventTimestamp = eventTimestamp;
    }

    public Long getMatchId() {
        return matchId;
    }

    public Long matchId() {
        return matchId;
    }

    public MatchEventType getType() {
        return type;
    }

    public MatchEventType type() {
        return type;
    }

    public int getOccurredAtSecond() {
        return occurredAtSecond;
    }

    public int occurredAtSecond() {
        return occurredAtSecond;
    }

    public double getValue() {
        return value;
    }

    public double value() {
        return value;
    }

    public long getEventTimestamp() {
        return eventTimestamp;
    }

    public long eventTimestamp() {
        return eventTimestamp;
    }
}
