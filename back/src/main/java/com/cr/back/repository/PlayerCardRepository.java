package com.cr.back.repository;

import com.cr.back.domain.PlayerCardEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlayerCardRepository extends JpaRepository<PlayerCardEntity, Long> {
    List<PlayerCardEntity> findByPlayerIdAndUnlocked(Long playerId, boolean unlocked);

    List<PlayerCardEntity> findByPlayerId(Long playerId);

    Optional<PlayerCardEntity> findByPlayerIdAndCardId(Long playerId, Long cardId);
}
