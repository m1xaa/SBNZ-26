import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormControl, FormGroup, NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { forkJoin, finalize } from 'rxjs';
import { CardService } from '../../services/card.service';
import { CardRequest, CardResponse, CardRole, CardType } from '../../models/card.model';
import { CardOption, PlaystyleOption } from '../../models/player-profile.model';
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
  protected readonly imagePreview = signal<string | null>(null);
  protected readonly imageBase64 = signal<string | null>(null);
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
    type: new FormControl<CardType | null>(null, [Validators.required]),
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
    this.imagePreview.set(card.image);
    this.imageBase64.set(null);
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
    this.imagePreview.set(null);
    this.imageBase64.set(null);
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
      this.errorMessage.set(this.getCardFormValidationMessage());
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
        next: ({ cards, cardOptions }) => {
          console.log(cards.find(c => c.imageAssetId === 122)); 
          this.cards.set(this.mergeImages(cards, cardOptions))
        },
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

  protected handleImageFileChange(event: Event): void {
    const file = (event.target as HTMLInputElement)?.files?.[0];
    if (!file) {
      this.imagePreview.set(null);
      this.imageBase64.set(null);
      return;
    }

    const reader = new FileReader();
    reader.onload = () => {
      const result = reader.result as string | ArrayBuffer | null;
      if (typeof result === 'string') {
        this.imagePreview.set(result);
        const base64 = result.startsWith('data:') ? result.split(',')[1] : result;
        this.imageBase64.set(base64);
      }
    };
    reader.readAsDataURL(file);
  }

  private toRequest(): CardRequest {
    const value = this.cardForm.getRawValue();
    return {
      name: value.name.trim(),
      imageBase64: this.imageBase64(),
      elixirCost: Number(value.elixirCost),
      type: value.type as CardType,
      roles: value.roles
    };
  }

  private getCardFormValidationMessage(): string {
    const nameControl = this.cardForm.controls.name;
    if (nameControl.invalid) {
      if (nameControl.errors?.['required']) {
        return 'Card name is required.';
      }
      if (nameControl.errors?.['maxlength']) {
        return 'Card name cannot exceed 80 characters.';
      }
    }

    const elixirControl = this.cardForm.controls.elixirCost;
    if (elixirControl.invalid) {
      if (elixirControl.errors?.['required']) {
        return 'Elixir cost is required.';
      }
      if (elixirControl.errors?.['min']) {
        return 'Elixir cost must be at least 0.5.';
      }
      if (elixirControl.errors?.['max']) {
        return 'Elixir cost cannot exceed 10.';
      }
    }

    const typeControl = this.cardForm.controls.type;
    if (typeControl.invalid && typeControl.errors?.['required']) {
      return 'Card type is required.';
    }

    return 'Please fix the highlighted form errors before saving.';
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
