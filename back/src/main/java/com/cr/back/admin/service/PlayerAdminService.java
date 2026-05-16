package com.cr.back.admin.service;

import com.cr.back.domain.deck.Archetype;
import com.cr.back.domain.player.PlayerEntity;
import com.cr.back.domain.player.PlayerPlaystyle;
import com.cr.back.repository.PlayerRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class PlayerAdminService {
    private final PlayerRepository playerRepository;

    public PlayerAdminService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    public List<PlayerEntity> findAll() {
        return playerRepository.findAll();
    }

    public PlayerEntity create(
            String username,
            PlayerPlaystyle playstyle,
            double maxPreferredAverageElixir,
            Archetype preferredArchetype,
            Set<String> dislikedCards
    ) {
        PlayerEntity player = new PlayerEntity(
                username,
                playstyle,
                maxPreferredAverageElixir,
                preferredArchetype
        );
        player.applyPlaystyle(playstyle);
        player.getDislikedCards().addAll(dislikedCards);
        return playerRepository.save(player);
    }

    public PlayerEntity updateProfile(
            Long playerId,
            String username,
            PlayerPlaystyle playstyle,
            double maxPreferredAverageElixir,
            Archetype preferredArchetype,
            Set<String> dislikedCards
    ) {
        PlayerEntity player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found: " + playerId));
        player.updateProfile(
                username,
                playstyle,
                maxPreferredAverageElixir,
                preferredArchetype,
                dislikedCards
        );
        player.applyPlaystyle(playstyle);
        return playerRepository.save(player);
    }
}
