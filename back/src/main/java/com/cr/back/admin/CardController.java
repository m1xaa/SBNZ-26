package com.cr.back.admin;

import com.cr.back.admin.dto.CardRequest;
import com.cr.back.admin.dto.CardResponse;
import com.cr.back.admin.service.CardAdminService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cards")
public class CardController {
    private final CardAdminService cardAdminService;

    public CardController(CardAdminService cardAdminService) {
        this.cardAdminService = cardAdminService;
    }

    @GetMapping
    public List<CardResponse> findAll() {
        return cardAdminService.findAll().stream()
                .map(CardResponse::from)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CardResponse create(@RequestBody CardRequest request) {
        return CardResponse.from(cardAdminService.create(
                request.name(),
                request.imageBase64(),
                request.elixirCost(),
                request.type(),
                request.roles()
        ));
    }

    @PutMapping("/{id}")
    public CardResponse update(@PathVariable Long id, @RequestBody CardRequest request) {
        return CardResponse.from(cardAdminService.update(
                id,
                request.name(),
                request.imageBase64(),
                request.elixirCost(),
                request.type(),
                request.roles()
        ));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        cardAdminService.delete(id);
    }
}
