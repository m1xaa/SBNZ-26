package com.cr.back.admin.service;

import com.cr.back.card.CardImageSupport;
import com.cr.back.domain.card.CardEntity;
import com.cr.back.domain.card.CardRole;
import com.cr.back.domain.card.CardType;
import com.cr.back.repository.CardRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Service
public class CardAdminService {
    private final CardRepository cardRepository;

    public CardAdminService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    public List<CardEntity> findAll() {
        return cardRepository.findAll();
    }

    public CardEntity create(String name, String imageBase64, double elixirCost, CardType type, Set<CardRole> roles) {
        Integer imageAssetId = nextImageAssetId();
        CardImageSupport.storeBase64Image(imageAssetId, imageBase64);
        return cardRepository.save(new CardEntity(name, imageAssetId, elixirCost, type, roles));
    }

    public CardEntity update(Long id, String name, String imageBase64, double elixirCost, CardType type, Set<CardRole> roles) {
        CardEntity card = cardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Card not found: " + id));
        Integer imageAssetId = card.getImageAssetId();
        if (imageBase64 != null && !imageBase64.isBlank()) {
            imageAssetId = imageAssetId == null ? nextImageAssetId() : imageAssetId;
            CardImageSupport.storeBase64Image(imageAssetId, imageBase64);
        }
        card.update(name, imageAssetId, elixirCost, type, roles);
        return cardRepository.save(card);
    }

    public void delete(Long id) {
        cardRepository.deleteById(id);
    }

    private Integer nextImageAssetId() {
        return cardRepository.findAll().stream()
                .map(CardEntity::getImageAssetId)
                .filter(value -> value != null)
                .max(Comparator.naturalOrder())
                .orElse(0) + 1;
    }
}
