import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormControl, FormGroup, NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { forkJoin, finalize } from 'rxjs';
import { CardRole } from '../../models/card.model';
import {
  Archetype,
  ArchetypeDefinitionResponse,
  CardOption,
  SynergyRequest,
  SynergyResponse,
  SynergyType,
  ValidationRuleResponse
} from '../../models/player-profile.model';
import { SelectionOptionsService } from '../../services/selection-options.service';

const SYNERGY_TYPES = ['COUNTER_SYNERGY', 'SYNERGY'] as const;
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
  selector: 'app-static-knowledge-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './static-knowledge.page.html',
  styleUrls: ['./static-knowledge.page.css']
})
export class StaticKnowledgePage implements OnInit {
  private readonly formBuilder = inject(NonNullableFormBuilder);
  private readonly optionsService = inject(SelectionOptionsService);

  protected readonly archetypes = signal<ArchetypeDefinitionResponse[]>([]);
  protected readonly validationRules = signal<ValidationRuleResponse[]>([]);
  protected readonly synergies = signal<SynergyResponse[]>([]);
  protected readonly cardOptions = signal<CardOption[]>([]);
  protected readonly loading = signal(true);
  protected readonly saving = signal(false);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly successMessage = signal<string | null>(null);
  protected readonly selectedSynergy = signal<SynergyResponse | null>(null);
  protected readonly synergyTypes = SYNERGY_TYPES;
  protected readonly cardRoles = CARD_ROLES;

  protected readonly synergyForm = this.formBuilder.group({
    cardAId: [null as number | null, [Validators.required]],
    cardBId: [null as number | null, [Validators.required]],
    type: [null as 'COUNTER_SYNERGY' | 'SYNERGY' | null, [Validators.required]],
    weight: [1, [Validators.required, Validators.min(0), Validators.max(100)]],
    explanation: ['', [Validators.required, Validators.maxLength(240)]]
  });

  ngOnInit(): void {
    this.loadStaticKnowledge();
  }

  protected get isSynergyEditing(): boolean {
    return this.selectedSynergy() !== null;
  }

  protected loadStaticKnowledge(): void {
    this.errorMessage.set(null);
    this.loading.set(true);

    forkJoin({
      archetypes: this.optionsService.getArchetypes(),
      validationRules: this.optionsService.getValidationRules(),
      synergies: this.optionsService.getSynergies(),
      cardOptions: this.optionsService.getCardOptions()
    })
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: ({ archetypes, validationRules, synergies, cardOptions }) => {
          this.archetypes.set(archetypes);
          this.validationRules.set(validationRules);
          this.synergies.set(synergies);
          this.cardOptions.set(cardOptions);
        },
        error: () => this.errorMessage.set('Could not load static knowledge from the backend.')
      });
  }

  protected editSynergy(item: SynergyResponse): void {
    this.errorMessage.set(null);
    this.successMessage.set(null);
    this.selectedSynergy.set(item);
    this.synergyForm.setValue({
      cardAId: item.cardAId,
      cardBId: item.cardBId,
      type: item.type,
      weight: item.weight,
      explanation: item.explanation
    });
  }

  protected resetSynergyForm(): void {
    this.selectedSynergy.set(null);
    this.synergyForm.reset({
      cardAId: null,
      cardBId: null,
      type: null,
      weight: 1,
      explanation: ''
    });
  }

  protected saveSynergy(): void {
    this.errorMessage.set(null);
    this.successMessage.set(null);

    if (this.synergyForm.invalid) {
      this.synergyForm.markAllAsTouched();
      this.errorMessage.set('Please fix the synergy form errors first.');
      return;
    }

    const value = this.synergyForm.getRawValue();
    const request: SynergyRequest = {
      cardAId: Number(value.cardAId),
      cardBId: Number(value.cardBId),
      type: value.type as SynergyType,
      weight: Number(value.weight),
      explanation: value.explanation.trim()
    };

    const operation = this.isSynergyEditing
      ? this.optionsService.updateSynergy(this.selectedSynergy()!.id, request)
      : this.optionsService.createSynergy(request);

    this.saving.set(true);
    operation.pipe(finalize(() => this.saving.set(false))).subscribe({
      next: (item) => {
        this.upsertSynergy(item);
        this.selectedSynergy.set(item);
        this.successMessage.set(this.isSynergyEditing ? 'Synergy updated.' : 'Synergy created.');
        this.resetSynergyForm();
      },
      error: (error) => {
        this.errorMessage.set(
          error?.message ?? 'Unable to save synergy. Confirm the backend is running.'
        );
      }
    });
  }

  protected cardLabel(cardId: number | null): string {
    return this.cardOptions().find((card) => card.id === cardId)?.name ?? 'Unknown card';
  }

  private upsertSynergy(item: SynergyResponse): void {
    const current = this.synergies();
    const existing = current.findIndex((entry) => entry.id === item.id);
    this.synergies.set(
      existing === -1 ? [...current, item] : current.map((entry) => (entry.id === item.id ? item : entry))
    );
  }
}
