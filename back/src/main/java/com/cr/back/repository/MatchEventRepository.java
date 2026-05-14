package com.cr.back.repository;

import com.cr.back.domain.MatchEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface MatchEventRepository extends JpaRepository<MatchEventEntity, Long> {
    List<MatchEventEntity> findByMatchIdIn(Collection<Long> matchIds);
}
