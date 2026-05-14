# AGENTS.md — Spring Boot Backend with Drools

## Project

This is the Spring Boot backend for a Clash Royale Deck Chooser expert system.

The backend recommends Clash Royale decks using:

- Spring Boot
- Spring Web
- Spring Data JPA
- PostgreSQL
- Lombok
- Drools
- Drools rule templates
- forward chaining
- CEP-style reasoning over match history and match events

The Angular frontend should only collect input and display output. All business reasoning belongs in this backend.

## Main Goal

Build a rule-based expert system that recommends one or more personalized Clash Royale decks based on:

- player style
- favorite cards
- disliked or excluded cards
- unlocked cards
- card levels
- recent match history
- match events
- player weaknesses
- archetype suitability
- card synergy and counter-synergy
- deck validity rules
- card replacement rules

The response must include a clear explanation of why the deck was recommended.

## Required Backend Architecture

Use a layered Spring Boot architecture.

```text
src/main/java/.../
  controller/
  service/
  repository/
  entity/
  dto/
  mapper/
  drools/
  config/
  exception/
```

Drools-specific code should be separated from normal service/entity code.

Recommended Drools package structure:

```text
src/main/java/.../drools/
  fact/
  result/
  service/
  util/
```

Drools rule files should be under:

```text
src/main/resources/rules/
```

Recommended rule resources:

```text
src/main/resources/rules/
  player-profile-rules.drl
  archetype-selection-rules.drl
  deck-candidate-rules.drl
  deck-validation-rules.drl
  card-selection-rules.drl
  replacement-rules.drl
  cep-match-history-rules.drl
```

Rule templates may go under:

```text
src/main/resources/templates/
  archetype-template.drt
  deck-validation-template.drt
  card-priority-template.drt
```

## Layer Responsibilities

### Controller

Controllers expose REST endpoints only.

Controllers should:

- receive DTO requests
- validate input
- call services
- return DTO responses

Controllers must not contain Drools logic.

Main endpoint:

```text
POST /api/deck-recommendations
```

Optional admin endpoints may exist under:

```text
/api/admin/cards
/api/admin/archetypes
/api/admin/synergies
/api/admin/rules
```

### Service

Services contain application flow.

Services may:

- validate business use cases
- load persisted data
- prepare Drools facts
- call Drools execution service
- map Drools results to DTO responses
- save recommendation history

Do not turn services into large if/else rule engines. Rule reasoning should be in Drools.

### Repository

Repositories handle database access only.

Use Spring Data JPA repositories.

### Entity

Entities represent persisted database tables.

Entities should not contain rule logic.

### DTO

DTOs are used for REST input and output.

Do not expose JPA entities directly in API responses.

### Mapper

Use mappers to convert between:

- DTOs
- entities
- Drools facts
- Drools result objects

Manual mappers are acceptable.

### Drools

Drools contains expert-system reasoning.

Rules should:

- infer player profile facts
- infer player weaknesses
- select or score archetypes
- form candidate decks
- select cards
- validate decks
- create replacements
- add explanations

## Suggested Entities

Use or adapt these entities.

```text
Player
Card
PlayerCard
CardSynergy
CardCounterSynergy
Archetype
ArchetypeRequirement
DeckTemplate
MatchHistory
MatchEvent
DeckRecommendation
RecommendedDeckCard
```

### Player

Suggested fields:

- id
- username
- preferredPlaystyle
- prefersAggressivePlay
- prefersControlPlay
- likesHeavyDecks
- preferredArchetype

### Card

Suggested fields:

- id
- name
- elixirCost
- rarity
- role
- type
- targetsAir
- spell
- smallSpell
- building
- winCondition
- tank
- splashSupport
- singleTargetDps
- cycleCard
- archetypeTags

### PlayerCard

Suggested fields:

- id
- player
- card
- unlocked
- level
- favorite
- disliked

### MatchHistory

Suggested fields:

- id
- player
- result
- opponentArchetype
- playerDeckArchetype
- averageElixir
- matchDate

### MatchEvent

Suggested fields:

- id
- matchHistory
- timeSeconds
- type
- value

Event examples:

- MATCH_FINISHED
- LOST_TOWER
- HIGH_ELIXIR_SPEND
- USED_HEAVY_DECK
- LOST_TO_AIR
- LOST_CONTROL

### DeckRecommendation

Suggested fields:

- id
- player
- archetype
- averageElixir
- score
- explanation
- createdAt

## Suggested DTOs

Use DTOs similar to:

```text
DeckRecommendationRequest
DeckRecommendationResponse
PlayerPreferencesDto
OwnedCardDto
MatchHistoryDto
MatchEventDto
RecommendedCardDto
CardReplacementDto
AdminCardDto
```

Example request DTO:

```java
public record DeckRecommendationRequest(
        PlayerPreferencesDto playerPreferences,
        List<OwnedCardDto> ownedCards,
        List<MatchHistoryDto> recentMatches,
        List<String> favoriteCards,
        List<String> excludedCards,
        String recommendationMode
) {
}
```

Example response DTO:

```java
public record DeckRecommendationResponse(
        String archetype,
        List<RecommendedCardDto> deck,
        Double averageElixir,
        Integer score,
        List<String> explanation,
        List<String> detectedWeaknesses,
        List<CardReplacementDto> replacements,
        List<String> warnings
) {
}
```

## Drools Facts

Keep Drools facts separate from JPA entities.

Recommended facts:

```text
PlayerProfileFact
OwnedCardFact
FavoriteCardFact
ExcludedCardFact
MatchHistoryFact
MatchEventFact
PlayerWeaknessFact
PlayerStyleFact
ArchetypeCandidateFact
DeckRequirementFact
DeckCandidateFact
SelectedCardFact
DeckValidationFact
CardReplacementFact
RecommendationExplanationFact
```

Facts should be simple Java objects.

Use Lombok where useful.

## Drools Result Object

Create a result object that collects rule output.

Example:

```java
@Getter
@Setter
public class DeckRecommendationResult {
    private String selectedArchetype;
    private List<SelectedCardFact> selectedCards = new ArrayList<>();
    private List<String> explanations = new ArrayList<>();
    private List<String> detectedWeaknesses = new ArrayList<>();
    private List<CardReplacementFact> replacements = new ArrayList<>();
    private List<String> warnings = new ArrayList<>();
    private Integer score;
    private Double averageElixir;
    private boolean valid;
}
```

Rules should add explanations whenever they make meaningful decisions.

## Drools Execution Service

Create a service similar to:

```text
DroolsDeckRecommendationService
```

Responsibilities:

- create a KieSession
- insert input facts
- insert a result object
- fire rules
- collect result
- dispose the session

Example flow:

```java
public DeckRecommendationResult recommend(DeckRecommendationInput input) {
    KieSession kieSession = kieContainer.newKieSession();

    try {
        DeckRecommendationResult result = new DeckRecommendationResult();
        kieSession.insert(result);
        input.getFacts().forEach(kieSession::insert);
        kieSession.fireAllRules();
        return result;
    } finally {
        kieSession.dispose();
    }
}
```

Always dispose the session.

## Forward Chaining Requirement

Do not jump directly from request input to final deck.

Use multi-step reasoning:

```text
input facts
  -> inferred player profile facts
  -> inferred weakness facts
  -> archetype candidate facts
  -> deck requirement facts
  -> selected card facts
  -> deck candidate fact
  -> validation facts
  -> recommendation result
```

Example chain:

```text
6+ losses against air in last 10 matches
  -> PlayerWeaknessFact("AIR")
  -> deck must contain at least two anti-air cards
  -> prioritize Firecracker or other anti-air cards
  -> validate anti-air coverage
  -> return explanation
```

## Required Rule Areas

### 1. Player Profile Rules

Implement rules such as:

1. If the player prefers fast gameplay, dislikes heavy decks, and most wins are with decks under 3.5 average elixir, infer cycle suitability.
2. If the player prefers slower and patient gameplay and performs well in longer matches, infer control or beatdown suitability.
3. If the player often wins recent matches with cheap decks, increase cycle priority.
4. If the player often loses recent matches with heavy decks, decrease beatdown priority.
5. If the player has 6 or more losses against air archetypes in the last 10 matches, infer air weakness.
6. If the player repeatedly loses after spending too much elixir without defense, infer overcommit weakness.

### 2. Archetype Selection Rules

Supported archetypes may include:

```text
CYCLE
HOG_CYCLE
CONTROL
BEATDOWN
BAIT
BRIDGE_SPAM
SIEGE
AIR_COUNTER
```

Implement rules such as:

- If cycle suitability is inferred, prioritize cycle and control archetypes.
- If slower style is inferred and the player has a high-level heavy win condition, prioritize beatdown.
- If aggressive bridge pressure is preferred and matching cards are available, prioritize bridge spam.
- If the player owns several cards that bait the same spell response, prioritize bait.
- If air weakness is detected, chosen archetypes must support at least two reliable anti-air cards.

### 3. Candidate Deck Formation Rules

Implement rules such as:

- If cycle is selected and a fast win condition is unlocked and upgraded, create a low-elixir cycle candidate.
- If beatdown is selected and a tank plus strong support are available, create a heavier beatdown candidate.
- If bait is selected and multiple spell-bait cards are available, create a bait candidate.
- If air weakness is detected, candidate decks must include at least two anti-air cards and one answer to air support.

### 4. Deck Validation Rules

Reject invalid decks when:

- deck does not have exactly 8 cards
- deck does not have exactly one main win condition
- deck has no small spell
- deck has no reliable answer to tanks or high single-target threats
- deck lacks at least two anti-air cards when air weakness is detected
- average elixir is too high for the player's habits and success profile
- key cards are locked or too low level compared to alternatives

### 5. Card Selection Rules

Implement rules such as:

- If Hog Cycle is selected and Hog Rider is one of the player's best upgraded win conditions, choose Hog Rider.
- If defensive building is required, prefer an unlocked, upgraded building that fits the deck.
- If air weakness is detected, prioritize Firecracker or another reliable upgraded anti-air card.
- If a small spell is required, prefer an unlocked, high-level, compatible spell.
- If cheap support is required, select cheap reliable cycle/support cards.
- If a card has strong synergy with the selected win condition, increase its priority.

### 6. Replacement Rules

Implement rules such as:

- If a key card is locked, find another card with the same role and similar function.
- If a deck is valid but still does not cover a detected weakness enough, replace one support or cycle slot.
- If two candidates satisfy the same requirements, choose the one with higher-level key cards.
- If the player excludes a card, preserve the deck logic and replace only that slot.

### 7. CEP / Match History Rules

Use CEP-style reasoning for recent match history and ordered match events.

Examples:

- 6 or more losses against air archetypes in the last 10 matches -> infer air weakness.
- Most wins in the last 8 matches with average elixir under 3.5 -> infer cycle suitability.
- Repeated high elixir spending followed by tower loss or loss of control -> infer overcommit weakness.
- Frequent losses with heavy decks -> reduce beatdown priority.

This can start with aggregated facts if full CEP is too complex, but the code should remain ready for event-based reasoning.

## Rule Templates

Use rule templates where repetitive rules exist.

Good candidates:

- archetype requirements
- average elixir ranges
- required card roles per archetype
- validation thresholds
- card priority scoring

Start with readable `.drl` files. Move repeated patterns into `.drt` templates later.

## Example API Payload

Request:

```json
{
  "playerPreferences": {
    "username": "player1",
    "preferredPlaystyle": "FAST",
    "dislikedHeavyDecks": true,
    "targetMode": "LADDER"
  },
  "ownedCards": [
    {
      "cardName": "Hog Rider",
      "level": 13,
      "unlocked": true
    },
    {
      "cardName": "Firecracker",
      "level": 13,
      "unlocked": true
    },
    {
      "cardName": "Cannon",
      "level": 12,
      "unlocked": true
    },
    {
      "cardName": "The Log",
      "level": 12,
      "unlocked": true
    },
    {
      "cardName": "Earthquake",
      "level": 12,
      "unlocked": true
    }
  ],
  "recentMatches": [
    {
      "result": "LOSS",
      "opponentArchetype": "AIR",
      "averageElixir": 3.1
    }
  ],
  "favoriteCards": ["Hog Rider", "Firecracker"],
  "excludedCards": []
}
```

Response:

```json
{
  "archetype": "HOG_CYCLE",
  "deck": [
    {
      "cardName": "Hog Rider",
      "role": "WIN_CONDITION",
      "reason": "Selected because it is unlocked, highly upgraded, and fits Hog Cycle."
    },
    {
      "cardName": "Firecracker",
      "role": "ANTI_AIR_SUPPORT",
      "reason": "Selected because recent match history shows weakness against air archetypes."
    }
  ],
  "averageElixir": 3.1,
  "score": 92,
  "explanation": [
    "Player prefers fast gameplay.",
    "Recent match history indicates weakness against air archetypes.",
    "Cycle archetype was prioritized because the player performs well with low-elixir decks.",
    "Hog Cycle was selected because Hog Rider is highly upgraded.",
    "Firecracker was prioritized to improve anti-air coverage."
  ],
  "detectedWeaknesses": ["AIR"],
  "replacements": [],
  "warnings": []
}
```

## Database

Use PostgreSQL with Spring Data JPA.

Suggested development properties:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/clash_deck_chooser
spring.datasource.username=postgres
spring.datasource.password=postgres

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

Seed card/archetype data simply at first.

Acceptable approaches:

- `data.sql`
- JSON import
- `CommandLineRunner`
- admin CRUD endpoints

## Admin Functionality

Administrator features may include CRUD for:

- cards
- card roles
- archetypes
- synergies
- counter-synergies
- deck templates
- validation thresholds
- card statistics

Keep admin CRUD separate from recommendation logic.

## Lombok

Use Lombok for boilerplate where appropriate:

```java
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
```

Java records are acceptable for DTOs.

## Testing

Add tests for:

- cycle style inference
- air weakness detection
- overcommit weakness detection
- archetype priority changes
- Hog Cycle selection
- deck validation
- locked card replacement
- excluded card replacement
- explanation generation
- controller request/response mapping

Prefer testing Drools behavior separately from controllers.

## Coding Rules

- Use controller/service/repository architecture.
- Use DTOs for API input and output.
- Use entities only for persistence.
- Keep Drools facts separate from JPA entities.
- Use mappers between DTOs, entities, facts, and result objects.
- Keep Drools rules readable and grouped by purpose.
- Add explanation strings for meaningful rule decisions.
- Avoid one huge service method with all recommendation logic.
- Avoid one huge `.drl` file if rules can be grouped.
- Always dispose `KieSession`.
- Do not expose entities directly in API responses.
- Do not modify Angular frontend files from this backend project.

## Development Priority

Implement in this order:

1. DTOs and recommendation endpoint.
2. Basic facts and result object.
3. Drools configuration.
4. Simple player profile rules.
5. Simple weakness detection rules.
6. Archetype candidate rules.
7. Card selection rules.
8. Deck validation rules.
9. Replacement rules.
10. Explanation output.
11. Persistence of recommendation history.
12. Admin CRUD if needed.
13. CEP/event-based improvements.
14. Rule templates.

Keep the first implementation structurally correct, then improve rule depth gradually.
