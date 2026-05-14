# AGENTS.md — Angular Frontend

## Project

This is the Angular frontend for a Clash Royale Deck Chooser application.

The backend is a Spring Boot + Drools expert system. The frontend should collect user input, call the backend API, and display recommendations and explanations.

The frontend must not implement rule logic. All expert-system reasoning belongs to the backend.

## Product Theme

The app recommends Clash Royale decks based on:

- player playstyle and preferences
- owned and unlocked cards
- card levels
- favorite and disliked cards
- recent match history
- weaknesses against archetypes, especially air decks
- preferred archetype or target mode
- backend-generated recommendation explanations
- possible card replacements

## Frontend Architecture

Frontend architecture is flexible. Keep it simple and practical.

Recommended structure:

```text
src/app/
  components/
  pages/
  services/
  models/
```

Use whatever Angular structure already exists in the project if it is reasonable.

## Backend API Assumptions

Backend local URL:

```text
http://localhost:8080
```

Backend endpoint prefix:

```text
/api
```

Main recommendation endpoint:

```text
POST /api/deck-recommendations
```

The frontend should call this endpoint and render the returned recommendation.

## Core Screens / Features

Implement UI for:

1. Player preferences
2. Owned card collection
3. Favorite cards
4. Disliked or excluded cards
5. Recent match history
6. Requesting deck recommendation
7. Viewing recommended deck
8. Viewing explanation of the recommendation
9. Viewing detected weaknesses
10. Viewing alternative card replacements

The explanation view is important because this project is an expert system.

## Suggested TypeScript Models

Keep models aligned with backend DTOs.

```ts
export interface DeckRecommendationRequest {
  playerPreferences: PlayerPreferences;
  ownedCards: OwnedCard[];
  recentMatches: MatchHistoryItem[];
  favoriteCards?: string[];
  excludedCards?: string[];
  recommendationMode?: 'MAIN_DECK' | 'MULTIPLE_ALTERNATIVES' | 'REPLACEMENTS_ONLY';
}

export interface PlayerPreferences {
  username?: string;
  preferredPlaystyle?: 'FAST' | 'BALANCED' | 'SLOW';
  prefersAggressivePlay?: boolean;
  prefersControlPlay?: boolean;
  likesHeavyDecks?: boolean;
  dislikedHeavyDecks?: boolean;
  preferredArchetype?: string;
  targetMode?: string;
}

export interface OwnedCard {
  cardName: string;
  level: number;
  unlocked: boolean;
}

export interface MatchHistoryItem {
  result: 'WIN' | 'LOSS';
  opponentArchetype: string;
  playerDeckArchetype?: string;
  averageElixir?: number;
  matchDate?: string;
  events?: MatchEventItem[];
}

export interface MatchEventItem {
  timeSeconds: number;
  type: string;
  value?: string;
}
```

```ts
export interface DeckRecommendationResponse {
  archetype: string;
  deck: RecommendedCard[];
  averageElixir: number;
  score?: number;
  explanation: string[];
  detectedWeaknesses: string[];
  replacements: CardReplacement[];
  warnings: string[];
}

export interface RecommendedCard {
  cardName: string;
  role: string;
  reason: string;
}

export interface CardReplacement {
  originalCard: string;
  replacementCard: string;
  reason: string;
}
```

## Angular Rules

- Prefer standalone components unless the existing project uses NgModules.
- Use TypeScript strict typing.
- Use Angular services for HTTP calls.
- Do not put HTTP logic directly inside components.
- Do not duplicate Drools/backend reasoning in Angular.
- Do not hardcode final deck recommendations when the backend endpoint exists.
- Components may manage UI state, but business decisions belong to the backend.
- Keep forms clear and simple.
- Use reactive forms for larger forms.
- Keep request and response interfaces synchronized with backend DTOs.

## Service Guidance

Create a service similar to:

```text
src/app/services/deck-recommendation.service.ts
```

It should expose methods like:

```ts
recommendDeck(request: DeckRecommendationRequest)
```

This service should call:

```text
POST http://localhost:8080/api/deck-recommendations
```

## UI Display Requirements

When showing a recommendation, display:

- selected archetype
- 8 recommended cards
- each card role
- each card reason
- average elixir
- detected player weaknesses
- warning if the collection is not good enough for the chosen archetype
- replacements for locked, low-level, or excluded cards
- step-by-step explanation from the backend

## Commands

Use existing scripts from `package.json`.

Common commands:

```bash
npm install
npm start
npm run build
npm test
```

## Do Not Do

- Do not modify backend files from the frontend project.
- Do not implement Drools rules in Angular.
- Do not store expert-system logic in components.
- Do not rename DTO fields unless the backend is changed too.
- Do not hide explanation data from the user.

## Development Priority

1. Define models.
2. Create API service.
3. Create recommendation form.
4. Render recommendation response.
5. Render explanation and weaknesses.
6. Add replacement display.
7. Improve styling only after the data flow works.
