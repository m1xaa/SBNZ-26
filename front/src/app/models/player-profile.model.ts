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
