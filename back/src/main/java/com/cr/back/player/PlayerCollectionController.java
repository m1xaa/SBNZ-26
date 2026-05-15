package com.cr.back.player;

import com.cr.back.domain.CardEntity;
import com.cr.back.domain.PlayerCardEntity;
import com.cr.back.domain.PlayerEntity;
import com.cr.back.repository.CardRepository;
import com.cr.back.repository.PlayerCardRepository;
import com.cr.back.repository.PlayerRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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

    @PostMapping("/card-levels")
    @Transactional
    public List<PlayerCardResponse> upsertCardLevels(
            @PathVariable Long playerId,
            @RequestBody List<CardLevelRequest> requests
    ) {
        PlayerEntity player = findPlayer(playerId);
        List<PlayerCardEntity> playerCards = requests.stream()
                .map(request -> upsertCardLevel(player, request.cardId(), request.level()))
                .toList();
        playerCardRepository.saveAll(playerCards);
        return collection(playerId);
    }

    @PatchMapping("/cards/{cardId}/level")
    @Transactional
    public PlayerCardResponse updateCardLevel(
            @PathVariable Long playerId,
            @PathVariable Long cardId,
            @RequestBody CardLevelUpdateRequest request
    ) {
        PlayerEntity player = findPlayer(playerId);
        PlayerCardEntity playerCard = upsertCardLevel(player, cardId, request.level());
        return PlayerCardResponse.from(playerCardRepository.save(playerCard));
    }

    private PlayerEntity findPlayer(Long playerId) {
        return playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found: " + playerId));
    }

    private PlayerCardEntity upsertCardLevel(PlayerEntity player, Long cardId, int level) {
        validateLevel(level);
        CardEntity card = cardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Card not found: " + cardId));
        PlayerCardEntity playerCard = playerCardRepository.findByPlayerIdAndCardId(player.getId(), cardId)
                .orElseGet(() -> new PlayerCardEntity(player, card, true, level, false));
        playerCard.updateLevel(level);
        return playerCard;
    }

    private void validateLevel(int level) {
        if (level < 1 || level > 15) {
            throw new IllegalArgumentException("Card level must be between 1 and 15.");
        }
    }

    public record PlayerCardRequest(Long cardId, boolean unlocked, int level, boolean reliablyUsed) {
    }

    public record CardLevelRequest(Long cardId, int level) {
    }

    public record CardLevelUpdateRequest(int level) {
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
