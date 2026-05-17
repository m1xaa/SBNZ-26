import { CardRole, CardType } from './card.model';

export type Archetype =
  | 'CYCLE'
  | 'CONTROL'
  | 'BEATDOWN'
  | 'BAIT'
  | 'BRIDGE_SPAM'
  | 'SIEGE'
  | 'AIR_COUNTER'
  | 'AIR_PRESSURE';

export type PlayerPlaystyle =
  | 'FAST_CYCLE'
  | 'TACTICAL_CONTROL'
  | 'AGGRESSIVE_BRIDGE_SPAM'
  | 'PATIENT_BEATDOWN'
  | 'BALANCED';

export interface PlayerProfileRequest {
  username: string;
  playstyle: PlayerPlaystyle;
  maxPreferredAverageElixir: number;
  preferredArchetype: Archetype | null;
  dislikedCards: string[];
}

export interface PlayerResponse {
  id: number;
  username: string;
  playstyle: PlayerPlaystyle;
  maxPreferredAverageElixir: number;
  preferredArchetype: Archetype | null;
  dislikedCards: string[];
}

export interface PlaystyleOption {
  playstyle: PlayerPlaystyle;
  displayName: string;
  description: string;
  defaultMaxPreferredAverageElixir: number;
}

export interface StaticOptionsResponse {
  archetypes: Archetype[];
  cardRoles: CardRole[];
  cardTypes: CardType[];
  matchOutcomes: MatchOutcome[];
  matchEventTypes: MatchEventType[];
  synergyTypes: string[];
  playerPlaystyles: PlayerPlaystyle[];
}

export interface ArchetypeDefinitionRequest {
  archetype: Archetype;
  description: string;
  minAverageElixir: number;
  maxAverageElixir: number;
  requiredRoles: CardRole[];
}

export interface ArchetypeDefinitionResponse {
  id: number;
  archetype: Archetype;
  description: string;
  minAverageElixir: number;
  maxAverageElixir: number;
  requiredRoles: CardRole[];
}

export interface ValidationRuleRequest {
  code: string;
  description: string;
  archetype: Archetype;
  active: boolean;
}

export interface ValidationRuleResponse {
  id: number;
  code: string;
  description: string;
  archetype: Archetype;
  active: boolean;
}

export type SynergyType = 'COUNTER_SYNERGY' | 'SYNERGY';

export interface SynergyRequest {
  cardAId: number;
  cardBId: number;
  type: SynergyType;
  weight: number;
  explanation: string;
}

export interface SynergyResponse {
  id: number;
  cardAId: number;
  cardAName: string;
  cardBId: number;
  cardBName: string;
  type: SynergyType;
  weight: number;
  explanation: string;
}

export interface CardOption {
  id: number;
  name: string;
  imageAssetId: number | null;
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
}

export interface PlayerCardResponse {
  cardId: number;
  cardName: string;
  unlocked: boolean;
  level: number;
}

export type MatchOutcome = 'WIN' | 'LOSS';

export type MatchEventType =
  | 'LARGE_ELIXIR_COMMIT'
  | 'TOWER_LOST'
  | 'CONTROL_LOST'
  | 'HUGE_DAMAGE_TAKEN';

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
  playedAt: string | null;
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
  level: number;
  image: string | null;
}

export interface DeckRecommendationResponse {
  playerId: number;
  username: string;
  archetype: Archetype;
  score: number;
  averageElixir: number;
  cards: RecommendedCard[];
  insights: string[];
  reasons: string[];
  warnings: string[];
  alternatives: Record<string, string>;
}
