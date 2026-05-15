package com.cr.back.player;

import com.cr.back.card.CardImageSupport;
import com.cr.back.domain.CardEntity;
import com.cr.back.domain.PlayerPlaystyleEntity;
import com.cr.back.repository.CardRepository;
import com.cr.back.repository.PlayerPlaystyleRepository;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class SelectionOptionsController {
    private final PlayerPlaystyleRepository playstyleRepository;
    private final CardRepository cardRepository;

    public SelectionOptionsController(PlayerPlaystyleRepository playstyleRepository, CardRepository cardRepository) {
        this.playstyleRepository = playstyleRepository;
        this.cardRepository = cardRepository;
    }

    @GetMapping("/playstyles")
    public List<PlaystyleOptionResponse> playstyles() {
        return playstyleRepository.findAll(Sort.by("name")).stream()
                .map(PlaystyleOptionResponse::from)
                .toList();
    }

    @GetMapping("/card-options")
    public List<CardOptionResponse> cardOptions() {
        return cardRepository.findAll(Sort.by("name")).stream()
                .map(CardOptionResponse::from)
                .toList();
    }

    public record PlaystyleOptionResponse(
            Long id,
            String code,
            String name,
            String description,
            boolean prefersFastGame,
            boolean likesHeavyDecks,
            boolean aggressivePressure,
            boolean patientGame,
            double defaultMaxPreferredAverageElixir
    ) {
        static PlaystyleOptionResponse from(PlayerPlaystyleEntity playstyle) {
            return new PlaystyleOptionResponse(
                    playstyle.getId(),
                    playstyle.getCode(),
                    playstyle.getName(),
                    playstyle.getDescription(),
                    playstyle.isPrefersFastGame(),
                    playstyle.isLikesHeavyDecks(),
                    playstyle.isAggressivePressure(),
                    playstyle.isPatientGame(),
                    playstyle.getDefaultMaxPreferredAverageElixir()
            );
        }
    }

    public record CardOptionResponse(
            Long id,
            String name,
            Integer imageAssetId,
            String image
    ) {
        static CardOptionResponse from(CardEntity card) {
            return new CardOptionResponse(
                    card.getId(),
                    card.getName(),
                    card.getImageAssetId(),
                    CardImageSupport.loadBase64Image(card.getImageAssetId())
            );
        }
    }
}
