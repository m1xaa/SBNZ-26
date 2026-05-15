import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { CardRequest, CardResponse } from '../models/card.model';

const API_BASE_URL = 'http://localhost:8080/api';

@Injectable({
  providedIn: 'root'
})
export class CardService {
  constructor(private readonly http: HttpClient) {}

  getCards(): Observable<CardResponse[]> {
    return this.http.get<CardResponse[]>(`${API_BASE_URL}/cards`);
  }

  createCard(request: CardRequest): Observable<CardResponse> {
    return this.http.post<CardResponse>(`${API_BASE_URL}/cards`, request);
  }

  updateCard(cardId: number, request: CardRequest): Observable<CardResponse> {
    return this.http.put<CardResponse>(`${API_BASE_URL}/cards/${cardId}`, request);
  }
}
