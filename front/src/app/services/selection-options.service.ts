import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { CardOption, PlaystyleOption } from '../models/player-profile.model';

const API_BASE_URL = 'http://localhost:8080/api';

@Injectable({
  providedIn: 'root'
})
export class SelectionOptionsService {
  constructor(private readonly http: HttpClient) {}

  getPlaystyles(): Observable<PlaystyleOption[]> {
    return this.http.get<PlaystyleOption[]>(`${API_BASE_URL}/playstyles`);
  }

  getCardOptions(): Observable<CardOption[]> {
    return this.http.get<CardOption[]>(`${API_BASE_URL}/card-options`);
  }
}
