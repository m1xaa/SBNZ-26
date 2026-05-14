package com.cr.back.repository;

import com.cr.back.domain.DeckValidationRuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeckValidationRuleRepository extends JpaRepository<DeckValidationRuleEntity, Long> {
}
