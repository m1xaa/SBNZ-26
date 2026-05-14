package com.cr.back.admin;

import com.cr.back.domain.CardEntity;
import com.cr.back.domain.CardRole;
import com.cr.back.domain.CardType;
import com.cr.back.repository.CardRepository;
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
import java.util.Set;

@RestController
@RequestMapping("/api/cards")
public class CardController {
    private final CardRepository cardRepository;

    public CardController(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    @GetMapping
    public List<CardResponse> findAll() {
        return cardRepository.findAll().stream()
                .map(CardResponse::from)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CardResponse create(@RequestBody CardRequest request) {
        CardEntity card = cardRepository.save(new CardEntity(
                request.name(),
                request.elixirCost(),
                request.type(),
                request.roles()
        ));
        return CardResponse.from(card);
    }

    @PutMapping("/{id}")
    public CardResponse update(@PathVariable Long id, @RequestBody CardRequest request) {
        CardEntity card = cardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Card not found: " + id));
        card.update(request.name(), request.elixirCost(), request.type(), request.roles());
        return CardResponse.from(cardRepository.save(card));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        cardRepository.deleteById(id);
    }

    public record CardRequest(String name, double elixirCost, CardType type, Set<CardRole> roles) {
    }

    public record CardResponse(Long id, String name, double elixirCost, CardType type, Set<CardRole> roles) {
        static CardResponse from(CardEntity card) {
            return new CardResponse(card.getId(), card.getName(), card.getElixirCost(), card.getType(), card.getRoles());
        }
    }
}
