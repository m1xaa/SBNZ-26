package com.cr.back.player;

import com.cr.back.domain.Archetype;
import com.cr.back.domain.MatchEntity;
import com.cr.back.domain.MatchEventEntity;
import com.cr.back.domain.MatchEventType;
import com.cr.back.domain.MatchOutcome;
import com.cr.back.domain.PlayerEntity;
import com.cr.back.repository.MatchEventRepository;
import com.cr.back.repository.MatchRepository;
import com.cr.back.repository.PlayerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/players/{playerId}/matches")
public class PlayerMatchController {
    private final PlayerRepository playerRepository;
    private final MatchRepository matchRepository;
    private final MatchEventRepository matchEventRepository;

    public PlayerMatchController(
            PlayerRepository playerRepository,
            MatchRepository matchRepository,
            MatchEventRepository matchEventRepository
    ) {
        this.playerRepository = playerRepository;
        this.matchRepository = matchRepository;
        this.matchEventRepository = matchEventRepository;
    }

    @GetMapping
    public List<MatchResponse> recentMatches(@PathVariable Long playerId) {
        return matchRepository.findTop10ByPlayerIdOrderByPlayedAtDesc(playerId).stream()
                .map(match -> MatchResponse.from(match, matchEventRepository.findByMatchIdIn(List.of(match.getId()))))
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public MatchResponse create(@PathVariable Long playerId, @RequestBody MatchRequest request) {
        PlayerEntity player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found: " + playerId));
        MatchEntity match = matchRepository.save(new MatchEntity(
                player,
                request.outcome(),
                request.opponentArchetype(),
                request.deckAverageElixir(),
                request.durationSeconds(),
                request.playedAt() == null ? Instant.now() : request.playedAt()
        ));
        List<MatchEventEntity> events = request.events().stream()
                .map(event -> new MatchEventEntity(match, event.type(), event.occurredAtSecond(), event.value()))
                .map(matchEventRepository::save)
                .toList();
        return MatchResponse.from(match, events);
    }

    public record MatchRequest(
            MatchOutcome outcome,
            Archetype opponentArchetype,
            double deckAverageElixir,
            int durationSeconds,
            Instant playedAt,
            List<MatchEventRequest> events
    ) {
        public MatchRequest {
            events = events == null ? List.of() : events;
        }
    }

    public record MatchEventRequest(MatchEventType type, int occurredAtSecond, double value) {
    }

    public record MatchResponse(
            Long id,
            MatchOutcome outcome,
            Archetype opponentArchetype,
            double deckAverageElixir,
            int durationSeconds,
            Instant playedAt,
            List<MatchEventResponse> events
    ) {
        static MatchResponse from(MatchEntity match, List<MatchEventEntity> events) {
            return new MatchResponse(
                    match.getId(),
                    match.getOutcome(),
                    match.getOpponentArchetype(),
                    match.getDeckAverageElixir(),
                    match.getDurationSeconds(),
                    match.getPlayedAt(),
                    events.stream().map(MatchEventResponse::from).toList()
            );
        }
    }

    public record MatchEventResponse(MatchEventType type, int occurredAtSecond, double value) {
        static MatchEventResponse from(MatchEventEntity event) {
            return new MatchEventResponse(event.getType(), event.getOccurredAtSecond(), event.getValue());
        }
    }
}
