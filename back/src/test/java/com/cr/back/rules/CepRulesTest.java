package com.cr.back.rules;

import com.cr.back.config.DroolsConfig;
import com.cr.back.domain.deck.Archetype;
import com.cr.back.domain.match.MatchEventType;
import com.cr.back.domain.match.MatchOutcome;
import com.cr.back.domain.player.PlayerPlaystyle;
import com.cr.back.rules.facts.ArchetypeScore;
import com.cr.back.rules.facts.MatchEventFact;
import com.cr.back.rules.facts.MatchFact;
import com.cr.back.rules.facts.OvercommitSequenceFact;
import com.cr.back.rules.facts.PlayerFact;
import com.cr.back.rules.facts.PlayerInsight;
import org.drools.core.time.SessionPseudoClock;
import org.junit.jupiter.api.Test;
import org.kie.api.runtime.ClassObjectFilter;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.core.io.DefaultResourceLoader;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CepRulesTest {

    @Test
    void detectsAirWeaknessFromSixRecentAirLosses() {
        KieSession session = newSession();
        try {
            insertPlayerAndScores(session, true, false);
            replay(session, List.of(
                match(1L, MatchOutcome.LOSS, Archetype.AIR_PRESSURE, 3.1, 0),
                match(2L, MatchOutcome.LOSS, Archetype.AIR_PRESSURE, 3.2, 60),
                match(3L, MatchOutcome.LOSS, Archetype.AIR_PRESSURE, 3.3, 120),
                match(4L, MatchOutcome.LOSS, Archetype.AIR_PRESSURE, 3.2, 180),
                match(5L, MatchOutcome.LOSS, Archetype.AIR_PRESSURE, 3.4, 240),
                match(6L, MatchOutcome.LOSS, Archetype.AIR_PRESSURE, 3.1, 300),
                    match(7L, MatchOutcome.WIN, Archetype.CYCLE, 3.1, 360),
                    match(8L, MatchOutcome.WIN, Archetype.CONTROL, 3.0, 420)
            ));

            assertInsight(session, "AIR_WEAKNESS");
        } finally {
            session.dispose();
        }
    }

    @Test
    void doesNotDetectAirWeaknessWithOnlyFiveAirLossesInLastTenMatches() {
        KieSession session = newSession();
        try {
            insertPlayerAndScores(session, true, false);
            replay(session, List.of(
                match(1L, MatchOutcome.LOSS, Archetype.AIR_PRESSURE, 3.1, 0),
                match(2L, MatchOutcome.LOSS, Archetype.AIR_PRESSURE, 3.2, 60),
                match(3L, MatchOutcome.LOSS, Archetype.AIR_PRESSURE, 3.3, 120),
                match(4L, MatchOutcome.LOSS, Archetype.AIR_PRESSURE, 3.2, 180),
                match(5L, MatchOutcome.LOSS, Archetype.AIR_PRESSURE, 3.4, 240),
                    match(6L, MatchOutcome.WIN, Archetype.CYCLE, 3.1, 300),
                    match(7L, MatchOutcome.WIN, Archetype.CONTROL, 3.0, 360),
                    match(8L, MatchOutcome.LOSS, Archetype.BEATDOWN, 4.1, 420),
                    match(9L, MatchOutcome.WIN, Archetype.BAIT, 3.2, 480),
                    match(10L, MatchOutcome.WIN, Archetype.SIEGE, 3.0, 540)
            ));

            assertNoInsight(session, "AIR_WEAKNESS");
        } finally {
            session.dispose();
        }
    }

    @Test
    void doesNotDetectAirWeaknessWhenNoTenMatchWindowReachesSixAirLosses() {
        KieSession session = newSession();
        try {
            insertPlayerAndScores(session, true, false);
            replay(session, List.of(
                match(1L, MatchOutcome.LOSS, Archetype.AIR_PRESSURE, 3.1, 0),
                    match(2L, MatchOutcome.WIN, Archetype.CYCLE, 3.1, 60),
                match(3L, MatchOutcome.LOSS, Archetype.AIR_PRESSURE, 3.3, 120),
                    match(4L, MatchOutcome.WIN, Archetype.CONTROL, 3.0, 180),
                match(5L, MatchOutcome.LOSS, Archetype.AIR_PRESSURE, 3.4, 240),
                    match(6L, MatchOutcome.WIN, Archetype.BEATDOWN, 4.2, 300),
                match(7L, MatchOutcome.LOSS, Archetype.AIR_PRESSURE, 3.1, 360),
                    match(8L, MatchOutcome.WIN, Archetype.BAIT, 3.2, 420),
                match(9L, MatchOutcome.LOSS, Archetype.AIR_PRESSURE, 3.2, 480),
                    match(10L, MatchOutcome.WIN, Archetype.SIEGE, 3.3, 540),
                    match(11L, MatchOutcome.WIN, Archetype.CONTROL, 3.1, 600)
            ));

            assertNoInsight(session, "AIR_WEAKNESS");
        } finally {
            session.dispose();
        }
    }

    @Test
    void detectsOvercommitFromTemporalEventSequence() {
        KieSession session = newSession();
        try {
            insertPlayerAndScores(session, false, false);
            List<Object> timeline = List.of(
                    match(1L, MatchOutcome.LOSS, Archetype.BEATDOWN, 4.2, 0),
                    event(1L, MatchEventType.LARGE_ELIXIR_COMMIT, 30, 8.0, 30),
                    event(1L, MatchEventType.TOWER_LOST, 50, 1.0, 50),
                    match(2L, MatchOutcome.WIN, Archetype.CYCLE, 3.0, 120),
                    match(3L, MatchOutcome.LOSS, Archetype.BRIDGE_SPAM, 4.1, 240),
                    event(3L, MatchEventType.LARGE_ELIXIR_COMMIT, 20, 8.0, 260),
                    event(3L, MatchEventType.HUGE_DAMAGE_TAKEN, 45, 700.0, 285)
            );
            replay(session, timeline);

            assertInsight(session, "OVERCOMMIT");
        } finally {
            session.dispose();
        }
    }

    @Test
    void doesNotDetectOvercommitWhenOnlyOneSequenceExists() {
        KieSession session = newSession();
        try {
            insertPlayerAndScores(session, false, false);
            replay(session, List.of(
                    match(1L, MatchOutcome.LOSS, Archetype.BEATDOWN, 4.2, 0),
                    event(1L, MatchEventType.LARGE_ELIXIR_COMMIT, 30, 8.0, 30),
                    event(1L, MatchEventType.TOWER_LOST, 50, 1.0, 50),
                    match(2L, MatchOutcome.WIN, Archetype.CYCLE, 3.0, 120),
                    match(3L, MatchOutcome.WIN, Archetype.BRIDGE_SPAM, 4.1, 240)
            ));

            assertNoInsight(session, "OVERCOMMIT");
        } finally {
            session.dispose();
        }
    }

    @Test
    void doesNotDetectOvercommitWhenFollowUpEventIsOutsideTemporalWindow() {
        KieSession session = newSession();
        try {
            insertPlayerAndScores(session, false, false);
            replay(session, List.of(
                    match(1L, MatchOutcome.LOSS, Archetype.BEATDOWN, 4.2, 0),
                    event(1L, MatchEventType.LARGE_ELIXIR_COMMIT, 10, 8.0, 10),
                    event(1L, MatchEventType.TOWER_LOST, 70, 1.0, 70),
                    match(2L, MatchOutcome.LOSS, Archetype.BRIDGE_SPAM, 4.1, 180),
                    event(2L, MatchEventType.LARGE_ELIXIR_COMMIT, 15, 8.0, 195),
                    event(2L, MatchEventType.CONTROL_LOST, 90, 1.0, 270)
            ));

            assertNoInsight(session, "OVERCOMMIT");
            assertEquals(0, countOvercommitSequences(session));
        } finally {
            session.dispose();
        }
    }

    @Test
    void doesNotDetectOvercommitFromWinningMatches() {
        KieSession session = newSession();
        try {
            insertPlayerAndScores(session, false, false);
            replay(session, List.of(
                    match(1L, MatchOutcome.WIN, Archetype.BEATDOWN, 4.2, 0),
                    event(1L, MatchEventType.LARGE_ELIXIR_COMMIT, 30, 8.0, 30),
                    event(1L, MatchEventType.TOWER_LOST, 50, 1.0, 50),
                    match(2L, MatchOutcome.WIN, Archetype.BRIDGE_SPAM, 4.1, 180),
                    event(2L, MatchEventType.LARGE_ELIXIR_COMMIT, 20, 8.0, 200),
                    event(2L, MatchEventType.CONTROL_LOST, 45, 1.0, 225)
            ));

            assertNoInsight(session, "OVERCOMMIT");
            assertEquals(0, countOvercommitSequences(session));
        } finally {
            session.dispose();
        }
    }

    @Test
    void createsOneOvercommitSequencePerMatch() {
        KieSession session = newSession();
        try {
            insertPlayerAndScores(session, false, false);
            replay(session, List.of(
                    match(1L, MatchOutcome.LOSS, Archetype.BEATDOWN, 4.2, 0),
                    event(1L, MatchEventType.LARGE_ELIXIR_COMMIT, 10, 8.0, 10),
                    event(1L, MatchEventType.TOWER_LOST, 20, 1.0, 20),
                    event(1L, MatchEventType.LARGE_ELIXIR_COMMIT, 35, 8.0, 35),
                    event(1L, MatchEventType.CONTROL_LOST, 45, 1.0, 45),
                    match(2L, MatchOutcome.LOSS, Archetype.BRIDGE_SPAM, 4.1, 180),
                    event(2L, MatchEventType.LARGE_ELIXIR_COMMIT, 20, 8.0, 200),
                    event(2L, MatchEventType.CONTROL_LOST, 45, 1.0, 225)
            ));

            assertInsight(session, "OVERCOMMIT");
            assertEquals(2, countOvercommitSequences(session));
        } finally {
            session.dispose();
        }
    }

    @Test
    void detectsCycleStyleFromCheapWinsWindow() {
        KieSession session = newSession();
        try {
            insertPlayerAndScores(session, true, false);
            replay(session, List.of(
                    match(1L, MatchOutcome.WIN, Archetype.BEATDOWN, 3.1, 0),
                    match(2L, MatchOutcome.WIN, Archetype.CONTROL, 3.2, 60),
                    match(3L, MatchOutcome.WIN, Archetype.BRIDGE_SPAM, 3.3, 120),
                match(4L, MatchOutcome.LOSS, Archetype.AIR_PRESSURE, 3.1, 180),
                    match(5L, MatchOutcome.WIN, Archetype.CYCLE, 3.4, 240),
                    match(6L, MatchOutcome.LOSS, Archetype.BEATDOWN, 4.1, 300)
            ));

            assertInsight(session, "CYCLE_STYLE");
            ArchetypeScore cycle = session.getObjects(new ClassObjectFilter(ArchetypeScore.class)).stream()
                    .map(ArchetypeScore.class::cast)
                    .filter(score -> score.getArchetype() == Archetype.CYCLE)
                    .findFirst()
                    .orElse(null);
            assertNotNull(cycle);
            assertTrue(cycle.getScore() > 0);
        } finally {
            session.dispose();
        }
    }

    @Test
    void doesNotDetectCycleStyleWithoutFastPreference() {
        KieSession session = newSession();
        try {
            insertPlayerAndScores(session, false, false);
            replay(session, List.of(
                    match(1L, MatchOutcome.WIN, Archetype.BEATDOWN, 3.1, 0),
                    match(2L, MatchOutcome.WIN, Archetype.CONTROL, 3.2, 60),
                    match(3L, MatchOutcome.WIN, Archetype.BRIDGE_SPAM, 3.3, 120),
                    match(4L, MatchOutcome.WIN, Archetype.CYCLE, 3.4, 180)
            ));

            assertNoInsight(session, "CYCLE_STYLE");
        } finally {
            session.dispose();
        }
    }

    @Test
    void doesNotDetectCycleStyleWhenCheapWinsAreNotMajorityOfRecentWins() {
        KieSession session = newSession();
        try {
            insertPlayerAndScores(session, true, false);
            replay(session, List.of(
                    match(1L, MatchOutcome.WIN, Archetype.BEATDOWN, 3.1, 0),
                    match(2L, MatchOutcome.WIN, Archetype.CONTROL, 3.2, 60),
                    match(3L, MatchOutcome.WIN, Archetype.BRIDGE_SPAM, 3.7, 120),
                    match(4L, MatchOutcome.WIN, Archetype.CYCLE, 3.8, 180),
                    match(5L, MatchOutcome.WIN, Archetype.BAIT, 4.0, 240),
                match(6L, MatchOutcome.LOSS, Archetype.AIR_PRESSURE, 3.1, 300)
            ));

            assertNoInsight(session, "CYCLE_STYLE");
        } finally {
            session.dispose();
        }
    }

    @Test
    void rewardsCycleScoreFromCheapWinsRule() {
        KieSession session = newSession();
        try {
            insertPlayerAndScores(session, true, false);
            replay(session, List.of(
                    match(1L, MatchOutcome.WIN, Archetype.BEATDOWN, 3.1, 0),
                    match(2L, MatchOutcome.WIN, Archetype.CONTROL, 3.2, 60),
                    match(3L, MatchOutcome.WIN, Archetype.BRIDGE_SPAM, 3.3, 120)
            ));

            ArchetypeScore cycle = getArchetypeScore(session, Archetype.CYCLE);
            assertNotNull(cycle);
            assertTrue(cycle.getScore() >= 35, "Expected fast baseline plus cheap-win boost for cycle.");
        } finally {
            session.dispose();
        }
    }

    @Test
    void penalizesBeatdownScoreAfterHeavyDeckLosses() {
        KieSession session = newSession();
        try {
            insertPlayerAndScores(session, false, true);
            replay(session, List.of(
                match(1L, MatchOutcome.LOSS, Archetype.AIR_PRESSURE, 4.1, 0),
                    match(2L, MatchOutcome.LOSS, Archetype.CYCLE, 4.2, 60),
                    match(3L, MatchOutcome.WIN, Archetype.CONTROL, 3.3, 120)
            ));

            ArchetypeScore beatdown = getArchetypeScore(session, Archetype.BEATDOWN);
            assertNotNull(beatdown);
            assertTrue(beatdown.getScore() < 0, "Expected heavy-deck losses to push beatdown below zero.");
        } finally {
            session.dispose();
        }
    }

    @Test
    void infersSlowStyleFromHeavyDeckPreference() {
        KieSession session = newSession();
        try {
            insertPlayerAndScores(session, false, true);
            replay(session, List.of());

            assertInsight(session, "SLOW_STYLE");
        } finally {
            session.dispose();
        }
    }

    private KieSession newSession() {
        KieContainer container;
        try {
            container = new DroolsConfig().kieContainer(new DefaultResourceLoader());
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to create KIE container for tests.", exception);
        }
        KieSession session = container.newKieSession("deckRecommendationSession");
        session.setGlobal("cards", List.of());
        return session;
    }

    private void insertPlayerAndScores(KieSession session, boolean fastGame, boolean likesHeavyDecks) {
        PlayerPlaystyle playstyle = likesHeavyDecks
                ? PlayerPlaystyle.PATIENT_BEATDOWN
                : fastGame
                ? PlayerPlaystyle.FAST_CYCLE
                : PlayerPlaystyle.BALANCED;
        session.insert(new PlayerFact(1L, "tester", playstyle, 3.6, null, Set.of()));
        for (Archetype archetype : Archetype.values()) {
            session.insert(new ArchetypeScore(archetype));
        }
    }

    private void replay(KieSession session, List<Object> facts) {
        SessionPseudoClock clock = session.getSessionClock();
        List<TimedFact> timeline = facts.stream()
                .map(fact -> new TimedFact(timestampOf(fact), fact))
                .sorted(Comparator.comparingLong(TimedFact::timestamp))
                .toList();

        long previous = timeline.isEmpty() ? 0L : timeline.getFirst().timestamp();
        for (TimedFact timedFact : timeline) {
            long delta = Math.max(0L, timedFact.timestamp() - previous);
            if (delta > 0L) {
                clock.advanceTime(delta, TimeUnit.MILLISECONDS);
            }
            session.insert(timedFact.fact());
            session.fireAllRules();
            previous = timedFact.timestamp();
        }
        session.fireAllRules();
    }

    private void assertInsight(KieSession session, String code) {
        boolean found = session.getObjects(new ClassObjectFilter(PlayerInsight.class)).stream()
                .map(PlayerInsight.class::cast)
                .anyMatch(insight -> insight.code().equals(code));
        assertTrue(found, "Expected insight " + code + " to be inferred.");
    }

    private void assertNoInsight(KieSession session, String code) {
        boolean found = session.getObjects(new ClassObjectFilter(PlayerInsight.class)).stream()
                .map(PlayerInsight.class::cast)
                .anyMatch(insight -> insight.code().equals(code));
        assertFalse(found, "Did not expect insight " + code + " to be inferred.");
    }

    private ArchetypeScore getArchetypeScore(KieSession session, Archetype archetype) {
        return session.getObjects(new ClassObjectFilter(ArchetypeScore.class)).stream()
                .map(ArchetypeScore.class::cast)
                .filter(score -> score.getArchetype() == archetype)
                .findFirst()
                .orElse(null);
    }

    private long countOvercommitSequences(KieSession session) {
        return session.getObjects(new ClassObjectFilter(OvercommitSequenceFact.class))
                .stream()
                .count();
    }

    private long timestampOf(Object fact) {
        if (fact instanceof MatchFact matchFact) {
            return matchFact.getPlayedAtEpochMillis();
        }
        if (fact instanceof MatchEventFact eventFact) {
            return eventFact.getEventTimestamp();
        }
        throw new IllegalArgumentException("Unsupported timed fact: " + fact.getClass().getName());
    }

    private MatchFact match(Long id, MatchOutcome outcome, Archetype opponentArchetype, double averageElixir, long second) {
        return new MatchFact(id, outcome, opponentArchetype, averageElixir, 180, TimeUnit.SECONDS.toMillis(second));
    }

    private MatchEventFact event(Long matchId, MatchEventType type, int occurredAtSecond, double value, long absoluteSecond) {
        return new MatchEventFact(matchId, type, occurredAtSecond, value, TimeUnit.SECONDS.toMillis(absoluteSecond));
    }

    private record TimedFact(long timestamp, Object fact) {
    }
}
