package com.cr.back.admin;

import com.cr.back.admin.dto.PlayerProfileRequest;
import com.cr.back.admin.dto.PlayerResponse;
import com.cr.back.admin.service.PlayerAdminService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/players")
public class PlayerController {
    private final PlayerAdminService playerAdminService;

    public PlayerController(PlayerAdminService playerAdminService) {
        this.playerAdminService = playerAdminService;
    }

    @GetMapping
    public List<PlayerResponse> findAll() {
        return playerAdminService.findAll().stream()
                .map(PlayerResponse::from)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlayerResponse create(@RequestBody PlayerProfileRequest request) {
        return PlayerResponse.from(playerAdminService.create(
                request.username(),
                request.playstyle(),
                request.maxPreferredAverageElixir(),
                request.preferredArchetype(),
                request.dislikedCards()
        ));
    }

    @PutMapping("/{playerId}/profile")
    public PlayerResponse updateProfile(@PathVariable Long playerId, @RequestBody PlayerProfileRequest request) {
        return PlayerResponse.from(playerAdminService.updateProfile(
                playerId,
                request.username(),
                request.playstyle(),
                request.maxPreferredAverageElixir(),
                request.preferredArchetype(),
                request.dislikedCards()
        ));
    }
}
