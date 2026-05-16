package com.cr.back.player.service;

import com.cr.back.domain.card.CardEntity;
import com.cr.back.domain.player.PlayerCardEntity;
import com.cr.back.domain.player.PlayerEntity;
import com.cr.back.repository.CardRepository;
import com.cr.back.repository.PlayerCardRepository;
import com.cr.back.repository.PlayerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PlayerCollectionService {
    private final PlayerRepository playerRepository;
    private final CardRepository cardRepository;
    private final PlayerCardRepository playerCardRepository;

    public PlayerCollectionService(
            PlayerRepository playerRepository,
            CardRepository cardRepository,
            PlayerCardRepository playerCardRepository
    ) {
        this.playerRepository = playerRepository;
        this.cardRepository = cardRepository;
        this.playerCardRepository = playerCardRepository;
    }

    public List<PlayerCardEntity> collection(Long playerId) {
        return playerCardRepository.findByPlayerId(playerId);
    }

    @Transactional
    public List<PlayerCardEntity> upsertCollection(Long playerId, List<PlayerCardUpdate> requests) {
        PlayerEntity player = findPlayer(playerId);
        for (PlayerCardUpdate request : requests) {
            CardEntity card = findCard(request.cardId());
            PlayerCardEntity playerCard = playerCardRepository.findByPlayerIdAndCardId(playerId, request.cardId())
                    .orElseGet(() -> new PlayerCardEntity(player, card, request.unlocked(), request.level()));
            playerCard.update(request.unlocked(), request.level());
            playerCardRepository.save(playerCard);
        }
        return collection(playerId);
    }

    @Transactional
    public List<PlayerCardEntity> upsertCardLevels(Long playerId, List<CardLevelUpdate> requests) {
        PlayerEntity player = findPlayer(playerId);
        List<PlayerCardEntity> playerCards = requests.stream()
                .map(request -> upsertCardLevel(player, request.cardId(), request.level()))
                .toList();
        playerCardRepository.saveAll(playerCards);
        return collection(playerId);
    }

    @Transactional
    public PlayerCardEntity updateCardLevel(Long playerId, Long cardId, int level) {
        PlayerEntity player = findPlayer(playerId);
        PlayerCardEntity playerCard = upsertCardLevel(player, cardId, level);
        return playerCardRepository.save(playerCard);
    }

    private PlayerEntity findPlayer(Long playerId) {
        return playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found: " + playerId));
    }

    private CardEntity findCard(Long cardId) {
        return cardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Card not found: " + cardId));
    }

    private PlayerCardEntity upsertCardLevel(PlayerEntity player, Long cardId, int level) {
        validateLevel(level);
        PlayerCardEntity playerCard = playerCardRepository.findByPlayerIdAndCardId(player.getId(), cardId)
                .orElseGet(() -> new PlayerCardEntity(player, findCard(cardId), true, level));
        playerCard.updateLevel(level);
        return playerCard;
    }

    private void validateLevel(int level) {
        if (level < 1 || level > 15) {
            throw new IllegalArgumentException("Card level must be between 1 and 15.");
        }
    }

    public record PlayerCardUpdate(Long cardId, boolean unlocked, int level) {
    }

    public record CardLevelUpdate(Long cardId, int level) {
    }
}
