package com.cr.back.rules.facts;

import org.kie.api.definition.type.Role;
import org.kie.api.definition.type.Timestamp;

@Role(Role.Type.EVENT)
@Timestamp("eventTimestamp")
public class OvercommitSequenceFact {
    private final Long matchId;
    private final long eventTimestamp;
    private final String explanation;

    public OvercommitSequenceFact(Long matchId, long eventTimestamp, String explanation) {
        this.matchId = matchId;
        this.eventTimestamp = eventTimestamp;
        this.explanation = explanation;
    }

    public Long getMatchId() {
        return matchId;
    }

    public long getEventTimestamp() {
        return eventTimestamp;
    }

    public String getExplanation() {
        return explanation;
    }
}
