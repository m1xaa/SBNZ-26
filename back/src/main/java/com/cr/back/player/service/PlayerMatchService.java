package com.cr.back.player.service;

import com.cr.back.domain.deck.Archetype;
import com.cr.back.domain.match.MatchEntity;
import com.cr.back.domain.match.MatchEventEntity;
import com.cr.back.domain.match.MatchEventType;
import com.cr.back.domain.match.MatchOutcome;
import com.cr.back.domain.player.PlayerEntity;
import com.cr.back.repository.MatchEventRepository;
import com.cr.back.repository.MatchRepository;
import com.cr.back.repository.PlayerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class PlayerMatchService {
    private final PlayerRepository playerRepository;
    private final MatchRepository matchRepository;
    private final MatchEventRepository matchEventRepository;

    public PlayerMatchService(
            PlayerRepository playerRepository,
            MatchRepository matchRepository,
            MatchEventRepository matchEventRepository
    ) {
        this.playerRepository = playerRepository;
        this.matchRepository = matchRepository;
        this.matchEventRepository = matchEventRepository;
    }

    public List<MatchWithEvents> recentMatches(Long playerId) {
        return matchRepository.findTop10ByPlayerIdOrderByPlayedAtDesc(playerId).stream()
                .map(match -> new MatchWithEvents(match, matchEventRepository.findByMatchIdIn(List.of(match.getId()))))
                .toList();
    }

    @Transactional
    public MatchWithEvents create(
            Long playerId,
            MatchOutcome outcome,
            Archetype opponentArchetype,
            double deckAverageElixir,
            int durationSeconds,
            Instant playedAt,
            List<MatchEventInput> events
    ) {
        PlayerEntity player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found: " + playerId));
        MatchEntity match = matchRepository.save(new MatchEntity(
                player,
                outcome,
                opponentArchetype,
                deckAverageElixir,
                durationSeconds,
                playedAt == null ? Instant.now() : playedAt
        ));
        List<MatchEventEntity> savedEvents = events.stream()
                .map(event -> new MatchEventEntity(match, event.type(), event.occurredAtSecond(), event.value()))
                .map(matchEventRepository::save)
                .toList();
        return new MatchWithEvents(match, savedEvents);
    }

    public record MatchEventInput(MatchEventType type, int occurredAtSecond, double value) {
    }

    public record MatchWithEvents(MatchEntity match, List<MatchEventEntity> events) {
    }
}
