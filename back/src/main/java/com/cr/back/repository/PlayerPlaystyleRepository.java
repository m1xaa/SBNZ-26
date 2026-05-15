package com.cr.back.repository;

import com.cr.back.domain.PlayerPlaystyleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlayerPlaystyleRepository extends JpaRepository<PlayerPlaystyleEntity, Long> {
    Optional<PlayerPlaystyleEntity> findByCode(String code);
}
