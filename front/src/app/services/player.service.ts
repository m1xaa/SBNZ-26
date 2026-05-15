import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  MatchRequest,
  MatchResponse,
  PlayerCardRequest,
  PlayerCardResponse,
  PlayerProfileRequest,
  PlayerResponse
} from '../models/player-profile.model';

const API_BASE_URL = 'http://localhost:8080/api';

@Injectable({
  providedIn: 'root'
})
export class PlayerService {
  constructor(private readonly http: HttpClient) {}

  getPlayers(): Observable<PlayerResponse[]> {
    return this.http.get<PlayerResponse[]>(`${API_BASE_URL}/players`);
  }

  createPlayer(request: PlayerProfileRequest): Observable<PlayerResponse> {
    return this.http.post<PlayerResponse>(`${API_BASE_URL}/players`, request);
  }

  updateProfile(playerId: number, request: PlayerProfileRequest): Observable<PlayerResponse> {
    return this.http.put<PlayerResponse>(`${API_BASE_URL}/players/${playerId}/profile`, request);
  }

  getPlayerCollection(playerId: number): Observable<PlayerCardResponse[]> {
    return this.http.get<PlayerCardResponse[]>(`${API_BASE_URL}/players/${playerId}/collection`);
  }

  updatePlayerCollection(playerId: number, request: PlayerCardRequest[]): Observable<PlayerCardResponse[]> {
    return this.http.put<PlayerCardResponse[]>(`${API_BASE_URL}/players/${playerId}/collection`, request);
  }

  getPlayerMatches(playerId: number): Observable<MatchResponse[]> {
    return this.http.get<MatchResponse[]>(`${API_BASE_URL}/players/${playerId}/matches`);
  }

  createPlayerMatch(playerId: number, request: MatchRequest): Observable<MatchResponse> {
    return this.http.post<MatchResponse>(`${API_BASE_URL}/players/${playerId}/matches`, request);
  }
}
