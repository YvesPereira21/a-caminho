import { ChangeDetectionStrategy, Component, computed, DestroyRef, ElementRef, inject, OnInit, signal, ViewChild } from '@angular/core';
import { NgTemplateOutlet } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { AuthService } from '../../../../core/auth/services/auth.service';
import { Bus } from '../../../../core/models';
import { BusService } from '../../services/bus.service';

type DialogMode = 'create' | 'edit' | 'view' | 'delete';

@Component({
  selector: 'app-municipality-buses',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, NgTemplateOutlet],
  templateUrl: './municipality-buses.component.html',
  styleUrl: './municipality-buses.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class MunicipalityBusesComponent implements OnInit {
  readonly auth = inject(AuthService);
  private readonly service = inject(BusService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly fb = inject(FormBuilder);
  @ViewChild('dialog') dialog!: ElementRef<HTMLDialogElement>;
  readonly buses = signal<Bus[]>([]);
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly loadError = signal(false);
  readonly error = signal('');
  readonly notice = signal('');
  readonly menuOpen = signal(false);
  readonly query = signal('');
  readonly capacity = signal('');
  readonly mode = signal<DialogMode>('create');
  readonly selected = signal<Bus | null>(null);
  readonly form = this.fb.nonNullable.group({
    busName: ['', [Validators.required, Validators.pattern(/.*\S.*/)]],
    seatsQuantity: [null as number | null, [Validators.required, Validators.min(1), Validators.pattern(/^\d+$/)]]
  });
  readonly municipalityLabel = computed(() => {
    const user = this.auth.currentUser();
    const name = user?.name?.trim();
    if (!name || name === user?.email || name === user?.email?.split('@')[0]) return 'Prefeitura';
    return /^prefeitura\b/i.test(name) ? name : `Prefeitura de ${name}`;
  });
  readonly capacities = computed(() => [...new Set(this.buses().map(bus => bus.seatsQuantity).filter(value => value != null))].sort((a, b) => a - b));
  readonly filtered = computed(() => {
    const query = this.normalize(this.query());
    return this.buses().filter(bus => this.normalize(bus.busName).includes(query) && (!this.capacity() || bus.seatsQuantity === Number(this.capacity())));
  });

  ngOnInit(): void { this.load(); }
  private normalize(value: string): string { return value.trim().normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLocaleLowerCase('pt-BR'); }
  load(): void {
    this.loading.set(true);
    this.loadError.set(false);
    this.service.getAllBusesByMunicipality().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: buses => { this.buses.set(buses); this.loading.set(false); },
      error: () => { this.loadError.set(true); this.loading.set(false); }
    });
  }
  open(mode: DialogMode, bus: Bus | null = null): void {
    this.mode.set(mode);
    this.selected.set(bus);
    this.error.set('');
    this.form.reset({ busName: bus?.busName ?? '', seatsQuantity: bus?.seatsQuantity ?? null });
    this.dialog.nativeElement.showModal();
  }
  close(): void { if (!this.saving()) this.dialog.nativeElement.close(); }
  save(): void {
    if (this.saving()) return;
    this.form.markAllAsTouched();
    if (this.form.invalid) return;
    const value = this.form.getRawValue();
    const payload = { busName: value.busName.trim(), seatsQuantity: value.seatsQuantity! };
    const selected = this.selected();
    const request = selected ? this.service.updateBus(selected.busId, payload) : this.service.createBus(payload);
    this.saving.set(true);
    this.error.set('');
    request.pipe(takeUntilDestroyed(this.destroyRef), finalize(() => this.saving.set(false))).subscribe({
      next: bus => {
        this.buses.update(items => selected ? items.map(item => item.busId === bus.busId ? bus : item) : [...items, bus]);
        this.notice.set(selected ? 'Ônibus atualizado.' : 'Ônibus cadastrado.');
        this.dialog.nativeElement.close();
      },
      error: (err: HttpErrorResponse) => this.error.set(err.error?.message || 'Não foi possível salvar o ônibus. Tente novamente.')
    });
  }
  remove(): void {
    const bus = this.selected();
    if (!bus || this.saving()) return;
    this.saving.set(true);
    this.error.set('');
    this.service.deleteBus(bus.busId).pipe(takeUntilDestroyed(this.destroyRef), finalize(() => this.saving.set(false))).subscribe({
      next: () => {
        this.buses.update(items => items.filter(item => item.busId !== bus.busId));
        if (this.capacity() && !this.capacities().includes(Number(this.capacity()))) this.capacity.set('');
        this.notice.set('Ônibus excluído.');
        this.dialog.nativeElement.close();
      },
      error: (err: HttpErrorResponse) => this.error.set(err.error?.message || 'Não foi possível excluir o ônibus. Tente novamente.')
    });
  }
}
