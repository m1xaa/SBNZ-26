import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { DeckRecommendationResponse } from '../models/player-profile.model';

const API_BASE_URL = 'http://localhost:8080/api';

@Injectable({
  providedIn: 'root'
})
export class DeckRecommendationService {
  constructor(private readonly http: HttpClient) {}

  recommendDeck(playerId: number): Observable<DeckRecommendationResponse> {
    return this.http.get<DeckRecommendationResponse>(`${API_BASE_URL}/recommendations/${playerId}`);
  }
}
