package com.cr.back.player;

import com.cr.back.player.dto.MatchRequest;
import com.cr.back.player.dto.MatchResponse;
import com.cr.back.player.service.PlayerMatchService;
import com.cr.back.player.service.PlayerMatchService.MatchEventInput;
import com.cr.back.player.service.PlayerMatchService.MatchWithEvents;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/players/{playerId}/matches")
public class PlayerMatchController {
    private final PlayerMatchService playerMatchService;

    public PlayerMatchController(PlayerMatchService playerMatchService) {
        this.playerMatchService = playerMatchService;
    }

    @GetMapping
    public List<MatchResponse> recentMatches(@PathVariable Long playerId) {
        return playerMatchService.recentMatches(playerId).stream()
                .map(PlayerMatchController::toResponse)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MatchResponse create(@PathVariable Long playerId, @RequestBody MatchRequest request) {
        return toResponse(playerMatchService.create(
                playerId,
                request.outcome(),
                request.opponentArchetype(),
                request.deckAverageElixir(),
                request.durationSeconds(),
                request.playedAt(),
                request.events().stream()
                        .map(event -> new MatchEventInput(event.type(), event.occurredAtSecond(), event.value()))
                        .toList()
        ));
    }

    private static MatchResponse toResponse(MatchWithEvents matchWithEvents) {
        return MatchResponse.from(matchWithEvents.match(), matchWithEvents.events());
    }
}
