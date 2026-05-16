package com.cr.back.player;

import com.cr.back.player.dto.CardOptionResponse;
import com.cr.back.player.dto.PlaystyleOptionResponse;
import com.cr.back.player.service.SelectionOptionsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class SelectionOptionsController {
    private final SelectionOptionsService selectionOptionsService;

    public SelectionOptionsController(SelectionOptionsService selectionOptionsService) {
        this.selectionOptionsService = selectionOptionsService;
    }

    @GetMapping("/playstyles")
    public List<PlaystyleOptionResponse> playstyles() {
        return selectionOptionsService.playstyles().stream()
                .map(PlaystyleOptionResponse::from)
                .toList();
    }

    @GetMapping("/card-options")
    public List<CardOptionResponse> cardOptions() {
        return selectionOptionsService.cardOptions().stream()
                .map(CardOptionResponse::from)
                .toList();
    }
}
