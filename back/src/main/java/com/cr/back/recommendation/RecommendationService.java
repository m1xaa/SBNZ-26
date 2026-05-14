package com.cr.back.recommendation;

import com.cr.back.domain.Archetype;
import com.cr.back.domain.CardEntity;
import com.cr.back.domain.MatchEntity;
import com.cr.back.domain.MatchEventEntity;
import com.cr.back.domain.PlayerCardEntity;
import com.cr.back.domain.PlayerEntity;
import com.cr.back.repository.CardRepository;
import com.cr.back.repository.MatchEventRepository;
import com.cr.back.repository.MatchRepository;
import com.cr.back.repository.PlayerCardRepository;
import com.cr.back.repository.PlayerRepository;
import com.cr.back.rules.facts.ArchetypeScore;
import com.cr.back.rules.facts.CardFact;
import com.cr.back.rules.facts.DeckCandidate;
import com.cr.back.rules.facts.MatchEventFact;
import com.cr.back.rules.facts.MatchFact;
import com.cr.back.rules.facts.PlayerFact;
import com.cr.back.rules.facts.PlayerInsight;
import org.kie.api.runtime.KieSession;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RecommendationService {
    private final PlayerRepository playerRepository;
    private final CardRepository cardRepository;
    private final PlayerCardRepository playerCardRepository;
    private final MatchRepository matchRepository;
    private final MatchEventRepository matchEventRepository;
    private final ObjectProvider<KieSession> kieSessionProvider;

    public RecommendationService(
            PlayerRepository playerRepository,
            CardRepository cardRepository,
            PlayerCardRepository playerCardRepository,
            MatchRepository matchRepository,
            MatchEventRepository matchEventRepository,
            ObjectProvider<KieSession> kieSessionProvider
    ) {
        this.playerRepository = playerRepository;
        this.cardRepository = cardRepository;
        this.playerCardRepository = playerCardRepository;
        this.matchRepository = matchRepository;
        this.matchEventRepository = matchEventRepository;
        this.kieSessionProvider = kieSessionProvider;
    }

    @Transactional(readOnly = true)
    public DeckRecommendationResponse recommend(Long playerId) {
        PlayerEntity player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found: " + playerId));

        List<PlayerCardEntity> ownedCards = playerCardRepository.findByPlayerId(playerId);
        Map<Long, PlayerCardEntity> ownershipByCardId = ownedCards.stream()
                .collect(Collectors.toMap(card -> card.getCard().getId(), card -> card));

        List<CardFact> cardFacts = cardRepository.findAll().stream()
                .map(card -> toCardFact(card, ownershipByCardId.get(card.getId())))
                .toList();
        List<MatchFact> matchFacts = matchRepository.findTop10ByPlayerIdOrderByPlayedAtDesc(playerId).stream()
                .map(this::toMatchFact)
                .toList();
        List<Long> matchIds = matchFacts.stream().map(MatchFact::id).toList();
        List<MatchEventFact> eventFacts = matchIds.isEmpty()
                ? List.of()
                : matchEventRepository.findByMatchIdIn(matchIds).stream().map(this::toMatchEventFact).toList();

        KieSession session = kieSessionProvider.getObject();
        try {
            session.setGlobal("cards", cardFacts);
            session.setGlobal("matches", matchFacts);
            session.setGlobal("events", eventFacts);
            session.insert(toPlayerFact(player));
            for (Archetype archetype : Archetype.values()) {
                session.insert(new ArchetypeScore(archetype));
            }
            cardFacts.forEach(session::insert);
            matchFacts.forEach(session::insert);
            eventFacts.forEach(session::insert);

            session.fireAllRules();

            List<PlayerInsight> insights = session.getObjects(object -> object instanceof PlayerInsight).stream()
                    .map(PlayerInsight.class::cast)
                    .toList();
            DeckCandidate best = session.getObjects(object -> object instanceof DeckCandidate).stream()
                    .map(DeckCandidate.class::cast)
                    .filter(DeckCandidate::isValid)
                    .max(Comparator.comparingInt(DeckCandidate::getScore)
                            .thenComparing(DeckCandidate::keyLevelSum)
                            .thenComparing(candidate -> -candidate.averageElixir()))
                    .orElseThrow(() -> new IllegalStateException("No valid deck candidate could be produced."));

            return new DeckRecommendationResponse(
                    player.getId(),
                    player.getUsername(),
                    best.getArchetype(),
                    best.getScore(),
                    best.averageElixir(),
                    best.getCards().stream().map(CardFact::name).toList(),
                    insights.stream().map(PlayerInsight::explanation).toList(),
                    best.getReasons(),
                    best.getWarnings(),
                    best.getAlternatives()
            );
        } finally {
            session.dispose();
        }
    }

    private PlayerFact toPlayerFact(PlayerEntity player) {
        return new PlayerFact(
                player.getId(),
                player.getUsername(),
                player.isPrefersFastGame(),
                player.isLikesHeavyDecks(),
                player.isAggressivePressure(),
                player.isPatientGame(),
                player.getMaxPreferredAverageElixir(),
                player.getPreferredArchetype(),
                Set.copyOf(player.getDislikedCards())
        );
    }

    private CardFact toCardFact(CardEntity card, PlayerCardEntity ownedCard) {
        return new CardFact(
                card.getName(),
                card.getElixirCost(),
                card.getType(),
                Set.copyOf(card.getRoles()),
                ownedCard != null && ownedCard.isUnlocked(),
                ownedCard == null ? 0 : ownedCard.getLevel(),
                ownedCard != null && ownedCard.isReliablyUsed()
        );
    }

    private MatchFact toMatchFact(MatchEntity match) {
        return new MatchFact(
                match.getId(),
                match.getOutcome(),
                match.getOpponentArchetype(),
                match.getDeckAverageElixir(),
                match.getDurationSeconds()
        );
    }

    private MatchEventFact toMatchEventFact(MatchEventEntity event) {
        return new MatchEventFact(
                event.getMatch().getId(),
                event.getType(),
                event.getOccurredAtSecond()
        );
    }
}
