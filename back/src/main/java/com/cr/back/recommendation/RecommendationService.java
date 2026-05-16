package com.cr.back.recommendation;

import com.cr.back.card.CardImageSupport;
import com.cr.back.domain.deck.Archetype;
import com.cr.back.domain.card.CardEntity;
import com.cr.back.domain.match.MatchEntity;
import com.cr.back.domain.match.MatchEventEntity;
import com.cr.back.domain.player.PlayerCardEntity;
import com.cr.back.domain.player.PlayerEntity;
import com.cr.back.repository.CardRepository;
import com.cr.back.repository.MatchEventRepository;
import com.cr.back.repository.MatchRepository;
import com.cr.back.repository.PlayerCardRepository;
import com.cr.back.repository.PlayerRepository;
import com.cr.back.recommendation.dto.DeckRecommendationResponse;
import com.cr.back.recommendation.dto.RecommendedCardResponse;
import com.cr.back.rules.facts.ArchetypeScore;
import com.cr.back.rules.facts.CardFact;
import com.cr.back.rules.facts.DeckCandidate;
import com.cr.back.rules.facts.DeckRecommendationResult;
import com.cr.back.rules.facts.MatchEventFact;
import com.cr.back.rules.facts.MatchFact;
import com.cr.back.rules.facts.PlayerFact;
import com.cr.back.rules.facts.PlayerInsight;
import org.drools.core.time.SessionPseudoClock;
import org.kie.api.runtime.KieSession;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
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

        List<PlayerCardEntity> ownedCards = playerCardRepository.findByPlayerIdAndUnlocked(playerId, true);
        Map<Long, PlayerCardEntity> ownershipByCardId = ownedCards.stream()
                .collect(Collectors.toMap(card -> card.getCard().getId(), card -> card));

        List<CardFact> cardFacts = cardRepository.findAll().stream()
                .map(card -> toCardFact(card, ownershipByCardId.get(card.getId())))
                .toList();
        List<MatchFact> matchFacts = matchRepository.findTop10ByPlayerIdOrderByPlayedAtDesc(playerId).stream()
                .map(this::toMatchFact)
                .toList();
        List<Long> matchIds = matchFacts.stream().map(MatchFact::getId).toList();
        List<MatchEventFact> eventFacts = matchIds.isEmpty()
                ? List.of()
                : matchEventRepository.findByMatchIdIn(matchIds).stream().map(this::toMatchEventFact).toList();

        KieSession session = kieSessionProvider.getObject();
        try {
            DeckRecommendationResult result = new DeckRecommendationResult();
            session.setGlobal("cards", cardFacts);
            session.insert(result);
            session.insert(toPlayerFact(player));
            for (Archetype archetype : Archetype.values()) {
                session.insert(new ArchetypeScore(archetype));
            }
            cardFacts.forEach(session::insert);
            replayHistoricalFacts(session, matchFacts, eventFacts);
            session.fireAllRules();

            DeckCandidate best = result.getBestCandidate();
            if (best == null) {
                throw new IllegalStateException("No deck candidate could be produced. Check unlocked cards and Drools rules.");
            }

            List<PlayerInsight> insights = session.getObjects(object -> object instanceof PlayerInsight).stream()
                    .map(PlayerInsight.class::cast)
                    .toList();

            List<String> warnings = new ArrayList<>(best.getWarnings());
            if (!best.isValid()) {
                warnings.add("No fully valid deck candidate was produced. Returning the strongest fallback candidate instead.");
            }

            return new DeckRecommendationResponse(
                    player.getId(),
                    player.getUsername(),
                    best.getArchetype(),
                    best.getScore(),
                    best.averageElixir(),
                    best.getCards().stream()
                            .map(card -> new RecommendedCardResponse(
                                    card.name(),
                                    card.level(),
                                    CardImageSupport.loadBase64Image(card.imageAssetId())
                            ))
                            .toList(),
                    insights.stream().map(PlayerInsight::explanation).toList(),
                    best.getReasons(),
                    warnings,
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
                player.getPlaystyle(),
                player.getMaxPreferredAverageElixir(),
                player.getPreferredArchetype(),
                Set.copyOf(player.getDislikedCards())
        );
    }

    private CardFact toCardFact(CardEntity card, PlayerCardEntity ownedCard) {
        return new CardFact(
                card.getName(),
                card.getImageAssetId(),
                card.getElixirCost(),
                card.getType(),
                Set.copyOf(card.getRoles()),
                ownedCard != null && ownedCard.isUnlocked(),
                ownedCard == null ? 0 : ownedCard.getLevel()
        );
    }

    private MatchFact toMatchFact(MatchEntity match) {
        return new MatchFact(
                match.getId(),
                match.getOutcome(),
                match.getOpponentArchetype(),
                match.getDeckAverageElixir(),
                match.getDurationSeconds(),
                match.getPlayedAt() == null ? 0L : match.getPlayedAt().toEpochMilli()
        );
    }

    private MatchEventFact toMatchEventFact(MatchEventEntity event) {
        long matchTimestamp = event.getMatch().getPlayedAt() == null ? 0L : event.getMatch().getPlayedAt().toEpochMilli();
        return new MatchEventFact(
                event.getMatch().getId(),
                event.getType(),
                event.getOccurredAtSecond(),
                event.getValue(),
                matchTimestamp + TimeUnit.SECONDS.toMillis(event.getOccurredAtSecond())
        );
    }

    private void replayHistoricalFacts(KieSession session, List<MatchFact> matchFacts, List<MatchEventFact> eventFacts) {
        SessionPseudoClock clock = session.getSessionClock();
        List<TimedInsertion> timeline = new ArrayList<>();
        matchFacts.forEach(match -> timeline.add(new TimedInsertion(match.getPlayedAtEpochMillis(), 0, match)));
        eventFacts.forEach(event -> timeline.add(new TimedInsertion(event.getEventTimestamp(), 1, event)));
        timeline.sort(Comparator
                .comparingLong(TimedInsertion::timestamp)
                .thenComparingInt(TimedInsertion::order));

        long previousTimestamp = timeline.isEmpty() ? 0L : timeline.getFirst().timestamp();
        for (TimedInsertion insertion : timeline) {
            long delta = Math.max(0L, insertion.timestamp() - previousTimestamp);
            if (delta > 0L) {
                clock.advanceTime(delta, TimeUnit.MILLISECONDS);
            }
            session.insert(insertion.fact());
            session.fireAllRules();
            previousTimestamp = insertion.timestamp();
        }
    }

    private record TimedInsertion(long timestamp, int order, Object fact) {
    }
}
