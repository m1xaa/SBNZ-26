package com.cr.back.admin;

import com.cr.back.domain.Archetype;
import com.cr.back.domain.PlayerEntity;
import com.cr.back.domain.PlayerPlaystyleEntity;
import com.cr.back.repository.PlayerPlaystyleRepository;
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
    private final PlayerPlaystyleRepository playstyleRepository;

    public PlayerController(PlayerRepository playerRepository, PlayerPlaystyleRepository playstyleRepository) {
        this.playerRepository = playerRepository;
        this.playstyleRepository = playstyleRepository;
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
        PlayerPlaystyleEntity playstyle = findPlaystyle(request.preferredPlaystyleId());
        PlayerEntity player = new PlayerEntity(
                request.username(),
                request.prefersFastGame(),
                request.likesHeavyDecks(),
                request.aggressivePressure(),
                request.patientGame(),
                request.maxPreferredAverageElixir(),
                playstyle,
                request.preferredArchetype()
        );
        player.applyPlaystyle(playstyle);
        player.getDislikedCards().addAll(request.dislikedCards());
        return PlayerResponse.from(playerRepository.save(player));
    }

    @PutMapping("/{playerId}/profile")
    public PlayerResponse updateProfile(@PathVariable Long playerId, @RequestBody PlayerProfileRequest request) {
        PlayerEntity player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found: " + playerId));
        PlayerPlaystyleEntity playstyle = findPlaystyle(request.preferredPlaystyleId());
        player.updateProfile(
                request.username(),
                request.prefersFastGame(),
                request.likesHeavyDecks(),
                request.aggressivePressure(),
                request.patientGame(),
                request.maxPreferredAverageElixir(),
                playstyle,
                request.preferredArchetype(),
                request.dislikedCards()
        );
        player.applyPlaystyle(playstyle);
        return PlayerResponse.from(playerRepository.save(player));
    }

    private PlayerPlaystyleEntity findPlaystyle(Long playstyleId) {
        if (playstyleId == null) {
            return null;
        }
        return playstyleRepository.findById(playstyleId)
                .orElseThrow(() -> new IllegalArgumentException("Player playstyle not found: " + playstyleId));
    }

    public record PlayerProfileRequest(
            String username,
            Long preferredPlaystyleId,
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
            PlayerPlaystyleResponse preferredPlaystyle,
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
                    PlayerPlaystyleResponse.from(player.getPreferredPlaystyle()),
                    player.getPreferredArchetype(),
                    player.getDislikedCards()
            );
        }
    }

    public record PlayerPlaystyleResponse(
            Long id,
            String code,
            String name,
            String description
    ) {
        static PlayerPlaystyleResponse from(PlayerPlaystyleEntity playstyle) {
            if (playstyle == null) {
                return null;
            }
            return new PlayerPlaystyleResponse(
                    playstyle.getId(),
                    playstyle.getCode(),
                    playstyle.getName(),
                    playstyle.getDescription()
            );
        }
    }
}
