# Frontend Contract

Use this as the API and data contract for the frontend.

## API DTOs

### `POST /api/cards`

```ts
type CardRequest = {
  name: string
  imageBase64: string | null
  elixirCost: number
  type: CardType
  roles: CardRole[]
}
```

### `GET/POST/PUT /api/cards`

```ts
type CardResponse = {
  id: number
  name: string
  imageAssetId: number | null
  image: string | null
  elixirCost: number
  type: CardType
  roles: CardRole[]
}
```

### `POST /api/players` and `PUT /api/players/{id}/profile`

```ts
type PlayerProfileRequest = {
  username: string
  playstyle: PlayerPlaystyle
  maxPreferredAverageElixir: number
  preferredArchetype: Archetype | null
  dislikedCards: string[]
}
```

### `GET /api/players`

```ts
type PlayerResponse = {
  id: number
  username: string
  playstyle: PlayerPlaystyle
  maxPreferredAverageElixir: number
  preferredArchetype: Archetype | null
  dislikedCards: string[]
}
```

### `GET /api/static/options`

```ts
type StaticOptionsResponse = {
  archetypes: Archetype[]
  cardRoles: CardRole[]
  cardTypes: CardType[]
  matchOutcomes: MatchOutcome[]
  matchEventTypes: MatchEventType[]
  synergyTypes: SynergyType[]
  playerPlaystyles: PlayerPlaystyle[]
}
```

### `POST/PUT /api/static/archetypes`

```ts
type ArchetypeDefinitionRequest = {
  archetype: Archetype
  description: string
  minAverageElixir: number
  maxAverageElixir: number
  requiredRoles: CardRole[]
}
```

### `POST/PUT /api/static/validation-rules`

```ts
type ValidationRuleRequest = {
  code: string
  description: string
  archetype: Archetype
  active: boolean
}
```

### `POST/PUT /api/static/synergies`

```ts
type SynergyRequest = {
  cardAId: number
  cardBId: number
  type: SynergyType
  weight: number
  explanation: string
}
```

### `GET /api/static/synergies`

```ts
type SynergyResponse = {
  id: number
  cardAId: number
  cardAName: string
  cardBId: number
  cardBName: string
  type: SynergyType
  weight: number
  explanation: string
}
```

### `PUT /api/players/{playerId}/collection`

```ts
type PlayerCardRequest = {
  cardId: number
  unlocked: boolean
  level: number
}
```

### `GET /api/players/{playerId}/collection`

```ts
type PlayerCardResponse = {
  cardId: number
  cardName: string
  unlocked: boolean
  level: number
}
```

### `POST /api/players/{playerId}/collection/card-levels`

```ts
type CardLevelRequest = {
  cardId: number
  level: number
}
```

### `PATCH /api/players/{playerId}/cards/{cardId}/level`

```ts
type CardLevelUpdateRequest = {
  level: number
}
```

### `POST /api/players/{playerId}/matches`

```ts
type MatchRequest = {
  outcome: MatchOutcome
  opponentArchetype: Archetype
  deckAverageElixir: number
  durationSeconds: number
  playedAt: string | null
  events: MatchEventRequest[]
}

type MatchEventRequest = {
  type: MatchEventType
  occurredAtSecond: number
  value: number
}
```

### `GET /api/players/{playerId}/matches`

```ts
type MatchResponse = {
  id: number
  outcome: MatchOutcome
  opponentArchetype: Archetype
  deckAverageElixir: number
  durationSeconds: number
  playedAt: string
  events: MatchEventResponse[]
}

type MatchEventResponse = {
  type: MatchEventType
  occurredAtSecond: number
  value: number
}
```

### `GET /api/playstyles`

```ts
type PlaystyleOptionResponse = {
  playstyle: PlayerPlaystyle
  displayName: string
  description: string
  defaultMaxPreferredAverageElixir: number
}
```

### `GET /api/card-options`

```ts
type CardOptionResponse = {
  id: number
  name: string
  imageAssetId: number | null
  image: string | null
}
```

### `GET /api/recommendations/{playerId}`

```ts
type DeckRecommendationResponse = {
  playerId: number
  username: string
  archetype: Archetype
  score: number
  averageElixir: number
  cards: RecommendedCardResponse[]
  insights: string[]
  reasons: string[]
  warnings: string[]
  alternatives: Record<string, string>
}

type RecommendedCardResponse = {
  name: string
  level: number
  image: string | null
}
```

## Entities Used

Frontend should not bind to these directly, but this is the backend persistence model.

```ts
type CardEntity = {
  id: number
  name: string
  imageAssetId: number | null
  elixirCost: number
  type: CardType
  roles: CardRole[]
}

type PlayerEntity = {
  id: number
  username: string
  playstyle: PlayerPlaystyle
  maxPreferredAverageElixir: number
  preferredArchetype: Archetype | null
  dislikedCards: string[]
}

type PlayerCardEntity = {
  id: number
  player: PlayerEntity
  card: CardEntity
  unlocked: boolean
  level: number
}

type MatchEntity = {
  id: number
  player: PlayerEntity
  outcome: MatchOutcome
  opponentArchetype: Archetype
  deckAverageElixir: number
  durationSeconds: number
  playedAt: string
}

type MatchEventEntity = {
  id: number
  match: MatchEntity
  type: MatchEventType
  occurredAtSecond: number
  value: number
}

type ArchetypeDefinitionEntity = {
  id: number
  archetype: Archetype
  description: string
  minAverageElixir: number
  maxAverageElixir: number
  requiredRoles: CardRole[]
}

type DeckValidationRuleEntity = {
  id: number
  code: string
  description: string
  archetype: Archetype
  active: boolean
}

type CardSynergyEntity = {
  id: number
  cardA: CardEntity
  cardB: CardEntity
  type: SynergyType
  weight: number
  explanation: string
}
```

## Enums

```ts
type PlayerPlaystyle =
  | "FAST_CYCLE"
  | "TACTICAL_CONTROL"
  | "AGGRESSIVE_BRIDGE_SPAM"
  | "PATIENT_BEATDOWN"
  | "BALANCED"

type MatchEventType =
  | "LARGE_ELIXIR_COMMIT"
  | "TOWER_LOST"
  | "CONTROL_LOST"
  | "HUGE_DAMAGE_TAKEN"
```

Other enums used across the app:

- `Archetype`
- `CardType`
- `CardRole`
- `MatchOutcome`
- `SynergyType`

## Notes

- Card create and update send `imageBase64`.
- Card/image reads return `image` already ready for `<img src=...>`.
- `ArchetypeDefinitionEntity` and `DeckValidationRuleEntity` are admin metadata, not recommendation result payloads.
