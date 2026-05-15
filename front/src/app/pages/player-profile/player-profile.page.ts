import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormControl, ReactiveFormsModule, Validators, NonNullableFormBuilder } from '@angular/forms';
import { finalize, forkJoin } from 'rxjs';
import {
  Archetype,
  CardOption,
  PlayerCard,
  PlayerCardRequest,
  PlayerCardResponse,
  PlayerProfileRequest,
  PlayerResponse,
  PlaystyleOption
} from '../../models/player-profile.model';
import { PlayerService } from '../../services/player.service';
import { SelectionOptionsService } from '../../services/selection-options.service';

const ARCHETYPES: Archetype[] = [
  'CYCLE',
  'CONTROL',
  'BEATDOWN',
  'BAIT',
  'BRIDGE_SPAM',
  'SIEGE',
  'AIR_COUNTER'
];

@Component({
  selector: 'app-player-profile-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './player-profile.page.html',
  styleUrls: ['./player-profile.page.css']
})
export class PlayerProfilePage implements OnInit {
  private readonly formBuilder = inject(NonNullableFormBuilder);
  private readonly playerService = inject(PlayerService);
  private readonly optionsService = inject(SelectionOptionsService);

  protected readonly archetypes = ARCHETYPES;
  protected readonly players = signal<PlayerResponse[]>([]);
  protected readonly playstyles = signal<PlaystyleOption[]>([]);
  protected readonly cardOptions = signal<CardOption[]>([]);
  protected readonly collection = signal<PlayerCard[]>([]);
  protected readonly loading = signal(true);
  protected readonly saving = signal(false);
  protected readonly collectionSaving = signal(false);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly successMessage = signal<string | null>(null);
  protected readonly savedProfile = signal<PlayerResponse | null>(null);

  protected readonly playerSelect = new FormControl<number | 'new'>('new', { nonNullable: true });

  protected readonly profileForm = this.formBuilder.group({
    username: ['', [Validators.required, Validators.maxLength(80)]],
    preferredPlaystyleId: new FormControl<number | null>(null),
    prefersFastGame: false,
    likesHeavyDecks: false,
    aggressivePressure: false,
    patientGame: false,
    maxPreferredAverageElixir: [4, [Validators.required, Validators.min(1), Validators.max(9)]],
    preferredArchetype: new FormControl<Archetype | null>(null),
    dislikedCards: this.formBuilder.control<string[]>([])
  });

  protected selectedPlaystyle(): PlaystyleOption | null {
    const playstyleId = this.profileForm.controls.preferredPlaystyleId.value;
    return this.playstyles().find((playstyle) => playstyle.id === playstyleId) ?? null;
  }

  protected selectedCollectionCount(): number {
    return this.collection().filter((item) => item.unlocked).length;
  }

  protected modeLabel(): string {
    return this.playerSelect.value === 'new' ? 'Create player profile' : 'Update player profile';
  }

  ngOnInit(): void {
    this.loadInitialData();

    this.playerSelect.valueChanges.subscribe((selection) => {
      this.successMessage.set(null);
      this.savedProfile.set(null);
      if (selection === 'new') {
        this.resetForm();
        this.resetCollection();
        return;
      }
      const player = this.players().find((item) => item.id === selection);
      if (player) {
        this.populateForm(player);
        this.loadPlayerCollection(player.id);
      }
    });

    this.profileForm.controls.preferredPlaystyleId.valueChanges.subscribe((playstyleId) => {
      const playstyle = this.playstyles().find((item) => item.id === playstyleId);
      if (!playstyle) {
        return;
      }
      this.profileForm.patchValue(
        {
          prefersFastGame: playstyle.prefersFastGame,
          likesHeavyDecks: playstyle.likesHeavyDecks,
          aggressivePressure: playstyle.aggressivePressure,
          patientGame: playstyle.patientGame,
          maxPreferredAverageElixir: playstyle.defaultMaxPreferredAverageElixir
        },
        { emitEvent: false }
      );
    });
  }

  protected saveProfile(): void {
    this.successMessage.set(null);
    this.errorMessage.set(null);

    if (this.profileForm.invalid) {
      this.profileForm.markAllAsTouched();
      this.errorMessage.set('Please fill the required profile fields before saving.');
      return;
    }

    const request = this.toRequest();
    const selectedPlayer = this.playerSelect.value;
    const saveRequest =
      selectedPlayer === 'new'
        ? this.playerService.createPlayer(request)
        : this.playerService.updateProfile(selectedPlayer, request);

    this.saving.set(true);
    saveRequest.pipe(finalize(() => this.saving.set(false))).subscribe({
      next: (profile) => {
        this.savedProfile.set(profile);
        this.successMessage.set(`Saved profile for ${profile.username}.`);
        this.upsertLocalPlayer(profile);
        this.playerSelect.setValue(profile.id, { emitEvent: false });
        this.populateForm(profile);
        this.loadPlayerCollection(profile.id);
      },
      error: () => {
        this.errorMessage.set('The backend could not save this profile. Check that it is running on port 8080.');
      }
    });
  }

  protected cardImageAlt(card: CardOption): string {
    return `${card.name} card`;
  }

  protected prettifyArchetype(archetype: Archetype): string {
    return archetype
      .toLowerCase()
      .split('_')
      .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
      .join(' ');
  }

  private loadInitialData(): void {
    this.loading.set(true);
    this.errorMessage.set(null);

    forkJoin({
      players: this.playerService.getPlayers(),
      playstyles: this.optionsService.getPlaystyles(),
      cardOptions: this.optionsService.getCardOptions()
    })
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: ({ players, playstyles, cardOptions }) => {
          this.players.set(players);
          this.playstyles.set(playstyles);
          this.cardOptions.set(cardOptions);
          this.collection.set(this.buildEmptyCollection(cardOptions));
          if (players.length > 0) {
            this.playerSelect.setValue(players[0].id);
          }
        },
        error: () => {
          this.errorMessage.set('Could not load profile data from http://localhost:8080/api.');
        }
      });
  }

  private populateForm(player: PlayerResponse): void {
    this.profileForm.reset({
      username: player.username,
      preferredPlaystyleId: player.preferredPlaystyle?.id ?? null,
      prefersFastGame: player.prefersFastGame,
      likesHeavyDecks: player.likesHeavyDecks,
      aggressivePressure: player.aggressivePressure,
      patientGame: player.patientGame,
      maxPreferredAverageElixir: player.maxPreferredAverageElixir,
      preferredArchetype: player.preferredArchetype,
      dislikedCards: player.dislikedCards ?? []
    });
  }

  private loadPlayerCollection(playerId: number): void {
    if (this.cardOptions().length === 0) {
      this.collection.set([]);
      return;
    }

    this.playerService.getPlayerCollection(playerId).subscribe({
      next: (playerCards) => {
        this.collection.set(this.mergeCollection(this.cardOptions(), playerCards));
      },
      error: () => {
        this.collection.set(this.buildEmptyCollection(this.cardOptions()));
      }
    });
  }

  private buildEmptyCollection(cardOptions: CardOption[]): PlayerCard[] {
    return cardOptions.map((card) => ({
      cardId: card.id,
      name: card.name,
      image: card.image,
      unlocked: false,
      level: 1,
      reliablyUsed: false
    }));
  }

  private mergeCollection(cardOptions: CardOption[], playerCards: PlayerCardResponse[]): PlayerCard[] {
    const playerMap = new Map(playerCards.map((item) => [item.cardId, item]));
    return cardOptions.map((card) => {
      const saved = playerMap.get(card.id);
      return {
        cardId: card.id,
        name: card.name,
        image: card.image,
        unlocked: saved?.unlocked ?? false,
        level: saved?.level ?? 1,
        reliablyUsed: saved?.reliablyUsed ?? false
      };
    });
  }

  protected resetCollection(): void {
    this.collection.set(this.buildEmptyCollection(this.cardOptions()));
  }

  protected saveCollection(): void {
    this.errorMessage.set(null);
    this.successMessage.set(null);

    const selectedPlayer = this.playerSelect.value;
    if (selectedPlayer === 'new') {
      this.errorMessage.set('Save the player profile first before updating collection.');
      return;
    }

    const request = this.collectionToRequest();
    this.collectionSaving.set(true);
    this.playerService
      .updatePlayerCollection(selectedPlayer, request)
      .pipe(finalize(() => this.collectionSaving.set(false)))
      .subscribe({
        next: () => {
          this.successMessage.set('Player collection saved.');
        },
        error: () => {
          this.errorMessage.set('Unable to save player collection. Confirm the backend is running on port 8080.');
        }
      });
  }

  private collectionToRequest(): PlayerCardRequest[] {
    return this.collection().map((card) => ({
      cardId: card.cardId,
      unlocked: card.unlocked,
      level: card.level,
      reliablyUsed: card.reliablyUsed
    }));
  }

  protected toggleCardUnlocked(card: PlayerCard, unlocked: boolean): void {
    this.collection.set(
      this.collection().map((item) =>
        item.cardId === card.cardId
          ? {
              ...item,
              unlocked,
              level: unlocked ? item.level : 1
            }
          : item
      )
    );
  }

  protected updateCardLevel(card: PlayerCard, level: number): void {
    const selectedLevel = Math.max(1, Math.min(16, Number(level)));
    this.collection.set(
      this.collection().map((item) =>
        item.cardId === card.cardId
          ? {
              ...item,
              unlocked: true,
              level: selectedLevel
            }
          : item
      )
    );
  }

  protected resetForm(): void {
    this.profileForm.reset({
      username: '',
      preferredPlaystyleId: null,
      prefersFastGame: false,
      likesHeavyDecks: false,
      aggressivePressure: false,
      patientGame: false,
      maxPreferredAverageElixir: 4,
      preferredArchetype: null,
      dislikedCards: []
    });
  }

  private toRequest(): PlayerProfileRequest {
    const value = this.profileForm.getRawValue();
    return {
      username: value.username.trim(),
      preferredPlaystyleId: value.preferredPlaystyleId,
      prefersFastGame: value.prefersFastGame,
      likesHeavyDecks: value.likesHeavyDecks,
      aggressivePressure: value.aggressivePressure,
      patientGame: value.patientGame,
      maxPreferredAverageElixir: Number(value.maxPreferredAverageElixir),
      preferredArchetype: value.preferredArchetype,
      dislikedCards: value.dislikedCards
    };
  }

  private upsertLocalPlayer(profile: PlayerResponse): void {
    const currentPlayers = this.players();
    const existingIndex = currentPlayers.findIndex((player) => player.id === profile.id);
    if (existingIndex === -1) {
      this.players.set([...currentPlayers, profile].sort((a, b) => a.username.localeCompare(b.username)));
      return;
    }
    this.players.set(currentPlayers.map((player) => (player.id === profile.id ? profile : player)));
  }
}
