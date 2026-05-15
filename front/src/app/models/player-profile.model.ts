export type Archetype =
  | 'CYCLE'
  | 'CONTROL'
  | 'BEATDOWN'
  | 'BAIT'
  | 'BRIDGE_SPAM'
  | 'SIEGE'
  | 'AIR_COUNTER';

export interface PlayerProfileRequest {
  username: string;
  preferredPlaystyleId: number | null;
  prefersFastGame: boolean;
  likesHeavyDecks: boolean;
  aggressivePressure: boolean;
  patientGame: boolean;
  maxPreferredAverageElixir: number;
  preferredArchetype: Archetype | null;
  dislikedCards: string[];
}

export interface PlayerResponse {
  id: number;
  username: string;
  prefersFastGame: boolean;
  likesHeavyDecks: boolean;
  aggressivePressure: boolean;
  patientGame: boolean;
  maxPreferredAverageElixir: number;
  preferredPlaystyle: PlayerPlaystyleResponse | null;
  preferredArchetype: Archetype | null;
  dislikedCards: string[];
}

export interface PlayerPlaystyleResponse {
  id: number;
  code: string;
  name: string;
  description: string;
}

export interface PlaystyleOption {
  id: number;
  code: string;
  name: string;
  description: string;
  prefersFastGame: boolean;
  likesHeavyDecks: boolean;
  aggressivePressure: boolean;
  patientGame: boolean;
  defaultMaxPreferredAverageElixir: number;
}

export interface CardOption {
  id: number;
  name: string;
  image: string | null;
}

export interface PlayerCard {
  cardId: number;
  name: string;
  image: string | null;
  unlocked: boolean;
  level: number;
  reliablyUsed: boolean;
}

export interface PlayerCardRequest {
  cardId: number;
  unlocked: boolean;
  level: number;
  reliablyUsed: boolean;
}

export interface PlayerCardResponse {
  cardId: number;
  cardName: string;
  unlocked: boolean;
  level: number;
  reliablyUsed: boolean;
}

export type MatchOutcome = 'WIN' | 'LOSS';

export type MatchEventType =
  | 'LARGE_ELIXIR_COMMIT'
  | 'TOWER_LOST'
  | 'CONTROL_LOST'
  | 'USED_CHEAP_DECK'
  | 'USED_HEAVY_DECK';

export interface MatchEventRequest {
  type: MatchEventType;
  occurredAtSecond: number;
  value: number;
}

export interface MatchEventResponse {
  type: MatchEventType;
  occurredAtSecond: number;
  value: number;
}

export interface MatchRequest {
  outcome: MatchOutcome;
  opponentArchetype: Archetype;
  deckAverageElixir: number;
  durationSeconds: number;
  playedAt?: string | null;
  events: MatchEventRequest[];
}

export interface MatchResponse {
  id: number;
  outcome: MatchOutcome;
  opponentArchetype: Archetype;
  deckAverageElixir: number;
  durationSeconds: number;
  playedAt: string;
  events: MatchEventResponse[];
}

export interface PlayerPreferences {
  username?: string;
  preferredPlaystyle?: string | null;
  prefersAggressivePlay?: boolean;
  prefersControlPlay?: boolean;
  likesHeavyDecks?: boolean;
  dislikedHeavyDecks?: boolean;
  preferredArchetype?: Archetype | null;
  targetMode?: string | null;
}

export interface OwnedCard {
  cardName: string;
  level: number;
  unlocked: boolean;
}

export interface MatchHistoryEvent {
  type: MatchEventType;
  occurredAtSecond: number;
  value: number;
}

export interface MatchHistoryItem {
  result: MatchOutcome;
  opponentArchetype: Archetype;
  playerDeckArchetype?: string | null;
  averageElixir?: number;
  matchDate?: string | null;
  events?: MatchHistoryEvent[];
}

export interface DeckRecommendationRequest {
  playerPreferences: PlayerPreferences;
  ownedCards: OwnedCard[];
  recentMatches: MatchHistoryItem[];
  favoriteCards?: string[];
  excludedCards?: string[];
  recommendationMode?: 'MAIN_DECK' | 'MULTIPLE_ALTERNATIVES' | 'REPLACEMENTS_ONLY';
}

export interface RecommendedCard {
  name: string;
  imageAssetId?: number | null;
  image?: string | null;
}

export interface DeckRecommendationResponse {
  playerId: number;
  username: string;
  archetype: Archetype;
  score: number;
  averageElixir: number;
  cards?: RecommendedCard[];
  insights?: string[];
  reasons?: string[];
  warnings?: string[];
  alternatives?: Record<string, string>;
}
