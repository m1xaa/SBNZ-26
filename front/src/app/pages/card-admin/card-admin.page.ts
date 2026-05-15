import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormControl, FormGroup, NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { forkJoin, finalize } from 'rxjs';
import { CardService } from '../../services/card.service';
import { CardRequest, CardResponse, CardRole, CardType } from '../../models/card.model';
import { CardOption } from '../../models/player-profile.model';
import { SelectionOptionsService } from '../../services/selection-options.service';

const CARD_TYPES: CardType[] = ['GROUND_TROOP', 'AIR_TROOP', 'SPELL', 'BUILDING'];
const CARD_ROLES: CardRole[] = [
  'WIN_CONDITION',
  'SMALL_SPELL',
  'BIG_SPELL',
  'BUILDING',
  'ANTI_AIR',
  'AIR_SUPPORT_COUNTER',
  'SPLASH_SUPPORT',
  'SINGLE_TARGET_DPS',
  'TANK',
  'SWARM',
  'CYCLE',
  'SUPPORT',
  'PRESSURE',
  'SIEGE'
];

@Component({
  selector: 'app-card-admin-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './card-admin.page.html',
  styleUrls: ['./card-admin.page.css']
})
export class CardAdminPage implements OnInit {
  private readonly formBuilder = inject(NonNullableFormBuilder);
  private readonly cardService = inject(CardService);
  private readonly optionsService = inject(SelectionOptionsService);

  protected readonly cardTypes = CARD_TYPES;
  protected readonly cardRoles = CARD_ROLES;
  protected readonly cards = signal<CardResponse[]>([]);
  protected readonly selectedCard = signal<CardResponse | null>(null);
  protected readonly loading = signal(true);
  protected readonly saving = signal(false);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly successMessage = signal<string | null>(null);

  protected readonly cardForm: FormGroup<{
    name: FormControl<string>;
    elixirCost: FormControl<number>;
    type: FormControl<CardType | null>;
    roles: FormControl<CardRole[]>;
  }> = this.formBuilder.group({
    name: ['', [Validators.required, Validators.maxLength(80)]],
    elixirCost: [1, [Validators.required, Validators.min(0.5), Validators.max(10)]],
    type: new FormControl<CardType | null>(null),
    roles: this.formBuilder.control<CardRole[]>([])
  });

  ngOnInit(): void {
    this.loadCards();
  }

  protected get isEditing(): boolean {
    return this.selectedCard() !== null;
  }

  protected selectCard(card: CardResponse): void {
    this.errorMessage.set(null);
    this.successMessage.set(null);
    this.selectedCard.set(card);
    this.cardForm.reset({
      name: card.name,
      elixirCost: card.elixirCost,
      type: card.type,
      roles: [...card.roles]
    });
  }

  protected resetForm(): void {
    this.selectedCard.set(null);
    this.errorMessage.set(null);
    this.successMessage.set(null);
    this.cardForm.reset({
      name: '',
      elixirCost: 1,
      type: null,
      roles: []
    });
  }

  protected saveCard(): void {
    this.errorMessage.set(null);
    this.successMessage.set(null);

    if (this.cardForm.invalid) {
      this.cardForm.markAllAsTouched();
      this.errorMessage.set('Please complete the card form before saving.');
      return;
    }

    const request = this.toRequest();
    const operation = this.isEditing
      ? this.cardService.updateCard(this.selectedCard()!.id, request)
      : this.cardService.createCard(request);

    this.saving.set(true);
    operation.pipe(finalize(() => this.saving.set(false))).subscribe({
      next: (card) => {
        this.successMessage.set(this.isEditing ? 'Card updated successfully.' : 'Card created successfully.');
        this.upsertLocalCard(card);
        this.selectedCard.set(card);
      },
      error: () => {
        this.errorMessage.set('Unable to save card. Confirm the backend is running on port 8080.');
      }
    });
  }

  private loadCards(): void {
    this.loading.set(true);
    forkJoin({
      cards: this.cardService.getCards(),
      cardOptions: this.optionsService.getCardOptions()
    })
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: ({ cards, cardOptions }) => this.cards.set(this.mergeImages(cards, cardOptions)),
        error: () => this.errorMessage.set('Could not load cards from the backend.')
      });
  }

  private mergeImages(cards: CardResponse[], cardOptions: CardOption[]): CardResponse[] {
    const optionsById = new Map(cardOptions.map((option) => [option.id, option.image]));
    return cards.map((card) => ({
      ...card,
      image: optionsById.get(card.id) ?? null
    }));
  }

  private toRequest(): CardRequest {
    const value = this.cardForm.getRawValue();
    return {
      name: value.name.trim(),
      elixirCost: Number(value.elixirCost),
      type: value.type as CardType,
      roles: value.roles
    };
  }

  private upsertLocalCard(card: CardResponse): void {
    const existing = this.cards().findIndex((item) => item.id === card.id);
    if (existing !== -1) {
      this.cards.set(this.cards().map((item) => (item.id === card.id ? card : item)));
      return;
    }
    this.cards.set([...this.cards(), card]);
  }
}
