package com.cr.back.player;

import com.cr.back.domain.CardEntity;
import com.cr.back.domain.PlayerCardEntity;
import com.cr.back.domain.PlayerEntity;
import com.cr.back.repository.CardRepository;
import com.cr.back.repository.PlayerCardRepository;
import com.cr.back.repository.PlayerRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/players/{playerId}/collection")
public class PlayerCollectionController {
    private final PlayerRepository playerRepository;
    private final CardRepository cardRepository;
    private final PlayerCardRepository playerCardRepository;

    public PlayerCollectionController(
            PlayerRepository playerRepository,
            CardRepository cardRepository,
            PlayerCardRepository playerCardRepository
    ) {
        this.playerRepository = playerRepository;
        this.cardRepository = cardRepository;
        this.playerCardRepository = playerCardRepository;
    }

    @GetMapping
    public List<PlayerCardResponse> collection(@PathVariable Long playerId) {
        return playerCardRepository.findByPlayerId(playerId).stream()
                .map(PlayerCardResponse::from)
                .toList();
    }

    @PutMapping
    @Transactional
    public List<PlayerCardResponse> upsertCollection(
            @PathVariable Long playerId,
            @RequestBody List<PlayerCardRequest> requests
    ) {
        PlayerEntity player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found: " + playerId));
        for (PlayerCardRequest request : requests) {
            CardEntity card = cardRepository.findById(request.cardId())
                    .orElseThrow(() -> new IllegalArgumentException("Card not found: " + request.cardId()));
            PlayerCardEntity playerCard = playerCardRepository.findByPlayerIdAndCardId(playerId, request.cardId())
                    .orElseGet(() -> new PlayerCardEntity(player, card, request.unlocked(), request.level(), request.reliablyUsed()));
            playerCard.update(request.unlocked(), request.level(), request.reliablyUsed());
            playerCardRepository.save(playerCard);
        }
        return collection(playerId);
    }

    public record PlayerCardRequest(Long cardId, boolean unlocked, int level, boolean reliablyUsed) {
    }

    public record PlayerCardResponse(
            Long cardId,
            String cardName,
            boolean unlocked,
            int level,
            boolean reliablyUsed
    ) {
        static PlayerCardResponse from(PlayerCardEntity entity) {
            return new PlayerCardResponse(
                    entity.getCard().getId(),
                    entity.getCard().getName(),
                    entity.isUnlocked(),
                    entity.getLevel(),
                    entity.isReliablyUsed()
            );
        }
    }
}
