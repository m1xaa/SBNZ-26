import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  ArchetypeDefinitionResponse,
  CardOption,
  PlaystyleOption,
  StaticOptionsResponse,
  SynergyRequest,
  SynergyResponse,
  ValidationRuleResponse
} from '../models/player-profile.model';

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

  getStaticOptions(): Observable<StaticOptionsResponse> {
    return this.http.get<StaticOptionsResponse>(`${API_BASE_URL}/static/options`);
  }

  getPlayerPlaystyles(): Observable<PlaystyleOption[]> {
    return this.http.get<PlaystyleOption[]>(`${API_BASE_URL}/static/player-playstyles`);
  }

  getArchetypes(): Observable<ArchetypeDefinitionResponse[]> {
    return this.http.get<ArchetypeDefinitionResponse[]>(`${API_BASE_URL}/static/archetypes`);
  }

  getValidationRules(): Observable<ValidationRuleResponse[]> {
    return this.http.get<ValidationRuleResponse[]>(`${API_BASE_URL}/static/validation-rules`);
  }

  getSynergies(): Observable<SynergyResponse[]> {
    return this.http.get<SynergyResponse[]>(`${API_BASE_URL}/static/synergies`);
  }

  createSynergy(request: SynergyRequest): Observable<SynergyResponse> {
    return this.http.post<SynergyResponse>(`${API_BASE_URL}/static/synergies`, request);
  }

  updateSynergy(id: number, request: SynergyRequest): Observable<SynergyResponse> {
    return this.http.put<SynergyResponse>(`${API_BASE_URL}/static/synergies/${id}`, request);
  }
}
