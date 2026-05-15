import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { DeckRecommendationService } from '../../services/deck-recommendation.service';
import { PlayerService } from '../../services/player.service';
import {
  DeckRecommendationResponse,
  MatchResponse,
  PlayerCardResponse,
  RecommendedCard,
  PlayerResponse
} from '../../models/player-profile.model';

type RecommendationViewModel = DeckRecommendationResponse & {
  cards: RecommendedCard[];
  insights: string[];
  reasons: string[];
  warnings: NonNullable<DeckRecommendationResponse['warnings']>;
  alternatives: Record<string, string>;
};

@Component({
  selector: 'app-deck-recommendation-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './deck-recommendation.page.html',
  styleUrls: ['./deck-recommendation.page.css']
})
export class DeckRecommendationPage implements OnInit {
  private readonly playerService = inject(PlayerService);
  private readonly recommendationService = inject(DeckRecommendationService);

  protected readonly players = signal<PlayerResponse[]>([]);
  protected readonly collection = signal<PlayerCardResponse[]>([]);
  protected readonly matches = signal<MatchResponse[]>([]);
  protected readonly recommendation = signal<RecommendationViewModel | null>(null);
  protected readonly loading = signal(true);
  protected readonly recommending = signal(false);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly selectedPlayer = new FormControl<number | 'new'>('new', { nonNullable: true });

  ngOnInit(): void {
    this.loadPlayers();

    this.selectedPlayer.valueChanges.subscribe((playerId) => {
      this.errorMessage.set(null);
      this.recommendation.set(null);

      if (playerId === 'new') {
        this.collection.set([]);
        this.matches.set([]);
        return;
      }

      this.loadPlayerData(playerId);
    });
  }

  protected recommendDeck(): void {
    this.errorMessage.set(null);
    this.recommendation.set(null);

    const playerId = this.selectedPlayer.value;
    if (playerId === 'new') {
      this.errorMessage.set('Choose a saved player before requesting a deck.');
      return;
    }

    this.recommending.set(true);

    this.recommendationService
      .recommendDeck(playerId)
      .pipe(finalize(() => this.recommending.set(false)))
      .subscribe({
        next: (response) => {
          this.recommendation.set(this.normalizeRecommendation(response));
        },
        error: () => this.errorMessage.set('Could not fetch deck recommendation. Make sure the backend is running on port 8080.')
      });
  }

  protected hasResults(): boolean {
    return this.recommendation() !== null;
  }

  protected getCardImageSrc(base64Image: string | null | undefined): string | null {
    if (!base64Image) {
      return null;
    }

    if (base64Image.startsWith('data:image/')) {
      return base64Image;
    }

    return `data:image/png;base64,${base64Image}`;
  }

  private loadPlayers(): void {
    this.loading.set(true);
    this.playerService.getPlayers().pipe(finalize(() => this.loading.set(false))).subscribe({
      next: (players) => {
        this.players.set(players);
      },
      error: () => this.errorMessage.set('Could not load players from the backend.')
    });
  }

  private loadPlayerData(playerId: number): void {
    this.playerService.getPlayerCollection(playerId).subscribe({
      next: (cards) => this.collection.set(cards),
      error: () => this.collection.set([])
    });

    this.playerService.getPlayerMatches(playerId).subscribe({
      next: (matches) => this.matches.set(matches),
      error: () => this.matches.set([])
    });
  }

  private normalizeRecommendation(response: DeckRecommendationResponse): RecommendationViewModel {
    return {
      ...response,
      cards: response.cards ?? [],
      insights: response.insights ?? [],
      reasons: response.reasons ?? [],
      warnings: response.warnings ?? [],
      alternatives: response.alternatives ?? {}
    };
  }
}
