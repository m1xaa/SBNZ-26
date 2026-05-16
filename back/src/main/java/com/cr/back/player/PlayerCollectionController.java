package com.cr.back.player;

import com.cr.back.player.dto.CardLevelRequest;
import com.cr.back.player.dto.CardLevelUpdateRequest;
import com.cr.back.player.dto.PlayerCardRequest;
import com.cr.back.player.dto.PlayerCardResponse;
import com.cr.back.player.service.PlayerCollectionService;
import com.cr.back.player.service.PlayerCollectionService.CardLevelUpdate;
import com.cr.back.player.service.PlayerCollectionService.PlayerCardUpdate;
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
    private final PlayerCollectionService playerCollectionService;

    public PlayerCollectionController(PlayerCollectionService playerCollectionService) {
        this.playerCollectionService = playerCollectionService;
    }

    @GetMapping
    public List<PlayerCardResponse> collection(@PathVariable Long playerId) {
        return playerCollectionService.collection(playerId).stream()
                .map(PlayerCardResponse::from)
                .toList();
    }

    @PutMapping
    public List<PlayerCardResponse> upsertCollection(
            @PathVariable Long playerId,
            @RequestBody List<PlayerCardRequest> requests
    ) {
        return playerCollectionService.upsertCollection(
                playerId,
                requests.stream()
                        .map(request -> new PlayerCardUpdate(request.cardId(), request.unlocked(), request.level()))
                        .toList()
        ).stream().map(PlayerCardResponse::from).toList();
    }

    @PostMapping("/card-levels")
    public List<PlayerCardResponse> upsertCardLevels(
            @PathVariable Long playerId,
            @RequestBody List<CardLevelRequest> requests
    ) {
        return playerCollectionService.upsertCardLevels(
                playerId,
                requests.stream()
                        .map(request -> new CardLevelUpdate(request.cardId(), request.level()))
                        .toList()
        ).stream().map(PlayerCardResponse::from).toList();
    }

    @PatchMapping("/cards/{cardId}/level")
    public PlayerCardResponse updateCardLevel(
            @PathVariable Long playerId,
            @PathVariable Long cardId,
            @RequestBody CardLevelUpdateRequest request
    ) {
        return PlayerCardResponse.from(playerCollectionService.updateCardLevel(playerId, cardId, request.level()));
    }
}
