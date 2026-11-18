import { ChangeDetectionStrategy, Component, computed, DestroyRef, ElementRef, inject, OnInit, signal, ViewChild } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { NgTemplateOutlet } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { AuthService } from '../../../../core/auth/services/auth.service';
import { BusDriverResponse } from '../../../../core/models';
import { BusDriverService } from '../../services/bus-driver.service';

@Component({
  selector: 'app-municipality-drivers', standalone: true,
  imports: [RouterLink, ReactiveFormsModule, NgTemplateOutlet],
  templateUrl: './municipality-drivers.component.html',
  styleUrl: './municipality-drivers.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class MunicipalityDriversComponent implements OnInit {
  readonly auth = inject(AuthService);
  private readonly service = inject(BusDriverService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly fb = inject(FormBuilder);
  @ViewChild('dialog') dialog!: ElementRef<HTMLDialogElement>;
  readonly drivers = signal<BusDriverResponse[]>([]);
  readonly loading = signal(true);
  readonly loadError = signal(false);
  readonly saving = signal(false);
  readonly error = signal('');
  readonly notice = signal('');
  readonly menuOpen = signal(false);
  readonly query = signal('');
  readonly field = signal('all');
  readonly selected = signal<BusDriverResponse | null>(null);
  readonly mode = signal<'create' | 'view'>('create');
  readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.pattern(/.*\S.*/)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6), Validators.pattern(/.*\S.*/s)]]
  });
  readonly municipalityLabel = computed(() => {
    const user = this.auth.currentUser();
    const name = user?.name?.trim();
    if (!name || name === user?.email || name === user?.email?.split('@')[0]) return 'Prefeitura';
    return /^prefeitura\b/i.test(name) ? name : `Prefeitura de ${name}`;
  });
  readonly filtered = computed(() => {
    const term = this.normalize(this.query());
    return this.drivers().filter(driver => {
      const values = this.field() === 'name' ? [driver.busDriverName] : this.field() === 'email' ? [driver.email] : this.field() === 'id' ? [driver.busDriverId] : [driver.busDriverName, driver.email, driver.busDriverId];
      return values.some(value => this.normalize(value).includes(term));
    });
  });
  private normalize(value: string): string { return value.trim().normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLocaleLowerCase('pt-BR'); }
  initials(name: string): string {
    const words = name.trim().split(/\s+/);
    return ((words[0]?.[0] ?? '') + (words.length > 1 ? words[words.length - 1][0] : '')).toLocaleUpperCase('pt-BR');
  }
  ngOnInit(): void { this.load(); }
  load(): void {
    this.loading.set(true); this.loadError.set(false);
    this.service.getAllBusDriversFromMunicipality().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: drivers => { this.drivers.set(drivers); this.loading.set(false); },
      error: () => { this.loadError.set(true); this.loading.set(false); }
    });
  }
  openCreate(): void {
    this.form.reset(); this.error.set(''); this.selected.set(null); this.mode.set('create');
    this.dialog.nativeElement.showModal();
  }
  openDetails(driver: BusDriverResponse): void {
    this.form.reset(); this.error.set(''); this.selected.set(driver); this.mode.set('view');
    this.dialog.nativeElement.showModal();
  }
  close(): void { if (!this.saving()) this.dialog.nativeElement.close(); }
  clearPassword(): void { this.form.controls.password.reset(); }
  save(): void {
    if (this.saving()) return;
    this.form.markAllAsTouched();
    if (this.form.invalid) return;
    const value = this.form.getRawValue();
    this.error.set(''); this.saving.set(true);
    this.service.createBusDriver({ busDriverName: value.name.trim(), user: { email: value.email.trim(), password: value.password } })
      .pipe(takeUntilDestroyed(this.destroyRef), finalize(() => this.saving.set(false)))
      .subscribe({
        next: driver => { this.drivers.update(items => [...items, driver]); this.notice.set('Motorista cadastrado.'); this.dialog.nativeElement.close(); this.clearPassword(); },
        error: (err: HttpErrorResponse) => this.error.set(err.error?.message || 'Não foi possível cadastrar o motorista. Tente novamente.')
      });
  }
}
