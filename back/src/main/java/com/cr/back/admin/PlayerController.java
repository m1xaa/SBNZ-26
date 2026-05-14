package com.cr.back.admin;

import com.cr.back.domain.Archetype;
import com.cr.back.domain.PlayerEntity;
import com.cr.back.repository.PlayerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/players")
public class PlayerController {
    private final PlayerRepository playerRepository;

    public PlayerController(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @GetMapping
    public List<PlayerResponse> findAll() {
        return playerRepository.findAll().stream()
                .map(PlayerResponse::from)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlayerResponse create(@RequestBody PlayerProfileRequest request) {
        PlayerEntity player = new PlayerEntity(
                request.username(),
                request.prefersFastGame(),
                request.likesHeavyDecks(),
                request.aggressivePressure(),
                request.patientGame(),
                request.maxPreferredAverageElixir(),
                request.preferredArchetype()
        );
        player.getDislikedCards().addAll(request.dislikedCards());
        return PlayerResponse.from(playerRepository.save(player));
    }

    @PutMapping("/{playerId}/profile")
    public PlayerResponse updateProfile(@PathVariable Long playerId, @RequestBody PlayerProfileRequest request) {
        PlayerEntity player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found: " + playerId));
        player.updateProfile(
                request.username(),
                request.prefersFastGame(),
                request.likesHeavyDecks(),
                request.aggressivePressure(),
                request.patientGame(),
                request.maxPreferredAverageElixir(),
                request.preferredArchetype(),
                request.dislikedCards()
        );
        return PlayerResponse.from(playerRepository.save(player));
    }

    public record PlayerProfileRequest(
            String username,
            boolean prefersFastGame,
            boolean likesHeavyDecks,
            boolean aggressivePressure,
            boolean patientGame,
            double maxPreferredAverageElixir,
            Archetype preferredArchetype,
            Set<String> dislikedCards
    ) {
        public PlayerProfileRequest {
            dislikedCards = dislikedCards == null ? Set.of() : dislikedCards;
        }
    }

    public record PlayerResponse(
            Long id,
            String username,
            boolean prefersFastGame,
            boolean likesHeavyDecks,
            boolean aggressivePressure,
            boolean patientGame,
            double maxPreferredAverageElixir,
            Archetype preferredArchetype,
            Set<String> dislikedCards
    ) {
        static PlayerResponse from(PlayerEntity player) {
            return new PlayerResponse(
                    player.getId(),
                    player.getUsername(),
                    player.isPrefersFastGame(),
                    player.isLikesHeavyDecks(),
                    player.isAggressivePressure(),
                    player.isPatientGame(),
                    player.getMaxPreferredAverageElixir(),
                    player.getPreferredArchetype(),
                    player.getDislikedCards()
            );
        }
    }
}
