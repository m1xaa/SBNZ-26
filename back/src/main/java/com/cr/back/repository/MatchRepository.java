package com.cr.back.repository;

import com.cr.back.domain.MatchEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatchRepository extends JpaRepository<MatchEntity, Long> {
    List<MatchEntity> findTop10ByPlayerIdOrderByPlayedAtDesc(Long playerId);
}
