export type CardType = 'GROUND_TROOP' | 'AIR_TROOP' | 'SPELL' | 'BUILDING';

export type CardRole =
  | 'WIN_CONDITION'
  | 'SMALL_SPELL'
  | 'BIG_SPELL'
  | 'BUILDING'
  | 'ANTI_AIR'
  | 'AIR_SUPPORT_COUNTER'
  | 'SPLASH_SUPPORT'
  | 'SINGLE_TARGET_DPS'
  | 'TANK'
  | 'SWARM'
  | 'CYCLE'
  | 'SUPPORT'
  | 'PRESSURE'
  | 'SIEGE';

export interface CardRequest {
  name: string;
  elixirCost: number;
  type: CardType;
  roles: CardRole[];
}

export interface CardResponse {
  id: number;
  name: string;
  elixirCost: number;
  type: CardType;
  roles: CardRole[];
  image?: string | null;
}
