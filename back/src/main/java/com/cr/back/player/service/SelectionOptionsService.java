package com.cr.back.player.service;

import com.cr.back.domain.card.CardEntity;
import com.cr.back.domain.player.PlayerPlaystyle;
import com.cr.back.repository.CardRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class SelectionOptionsService {
    private final CardRepository cardRepository;

    public SelectionOptionsService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    public List<PlayerPlaystyle> playstyles() {
        return Arrays.asList(PlayerPlaystyle.values());
    }

    public List<CardEntity> cardOptions() {
        return cardRepository.findAll(Sort.by("name"));
    }
}
