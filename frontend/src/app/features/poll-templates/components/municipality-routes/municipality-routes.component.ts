import { ChangeDetectionStrategy, Component, computed, DestroyRef, ElementRef, inject, OnInit, signal, ViewChild } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpErrorResponse } from '@angular/common/http';
import { LeafletModule } from '@bluehalo/ngx-leaflet';
import * as L from 'leaflet';
import { finalize, forkJoin, Subscription } from 'rxjs';
import { AuthService } from '../../../../core/auth/services/auth.service';
import { PollOptionResponse, PollTemplate, Shift, UniversityResponse } from '../../../../core/models';
import { PollTemplateService } from '../../services/poll-template.service';
import { PollOptionService } from '../../../poll-options/services/poll-option.service';
import { UniversityService } from '../../../universities/services/university.service';

@Component({
  selector: 'app-municipality-routes', standalone: true,
  imports: [RouterLink, ReactiveFormsModule, LeafletModule],
  templateUrl: './municipality-routes.component.html', styleUrl: './municipality-routes.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class MunicipalityRoutesComponent implements OnInit {
  readonly auth = inject(AuthService);
  private readonly service = inject(PollTemplateService);
  private readonly optionsService = inject(PollOptionService);
  private readonly universityService = inject(UniversityService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly fb = inject(FormBuilder);
  @ViewChild('dialog') dialog!: ElementRef<HTMLDialogElement>;
  readonly routes = signal<PollTemplate[]>([]);
  readonly options = signal<PollOptionResponse[]>([]);
  readonly universities = signal<UniversityResponse[]>([]);
  readonly selectedId = signal<string | null>(null);
  readonly loading = signal(true);
  readonly loadError = signal(false);
  readonly formDataError = signal(false);
  readonly formLoading = signal(false);
  readonly saving = signal(false);
  readonly error = signal('');
  readonly notice = signal('');
  readonly menuOpen = signal(false);
  readonly query = signal('');
  readonly shift = signal('');
  readonly status = signal('');
  readonly editing = signal<PollTemplate | null>(null);
  readonly selectedStops = signal<string[]>([]);
  readonly selectedUniversities = signal<string[]>([]);
  readonly form = this.fb.nonNullable.group({
    routeName: ['', [Validators.required, Validators.pattern(/.*\S.*/)]],
    shift: ['', Validators.required], start: ['', Validators.required], end: ['', Validators.required]
  });
  readonly municipalityLabel = computed(() => {
    const user = this.auth.currentUser(); const name = user?.name?.trim();
    if (!name || name === user?.email || name === user?.email?.split('@')[0]) return 'Prefeitura';
    return /^prefeitura\b/i.test(name) ? name : `Prefeitura de ${name}`;
  });
  readonly filtered = computed(() => {
    const q = this.normalize(this.query());
    return this.routes().filter(route => (!this.shift() || this.normalize(String(route.shift)) === this.normalize(this.shift())) &&
      (!this.status() || String(route.active) === this.status()) &&
      this.normalize([route.routeName, ...route.defaultOptions.map(stop => stop.stopName)].join(' ')).includes(q));
  });
  readonly selected = computed<PollTemplate | null>(() => this.filtered().find(route => route.templateId === this.selectedId()) ?? this.filtered()[0] ?? null);
  readonly mapOptions: L.MapOptions = { layers: [L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', { maxZoom: 19, attribution: '&copy; OpenStreetMap contributors' })], center: L.latLng(-7.12, -36.72), zoom: 8 };
  private map: L.Map | null = null;
  private layers: L.Layer[] = [];
  private formRequest?: Subscription;
  private normalize(value: string): string { return value.trim().normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLocaleLowerCase('pt-BR'); }
  ngOnInit(): void { this.load(); }
  load(): void {
    this.loading.set(true); this.loadError.set(false);
    this.service.getAllPollTemplates().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: routes => { this.routes.set(routes); this.loading.set(false); },
      error: () => { this.loadError.set(true); this.loading.set(false); }
    });
  }
  filterChanged(): void {
    if (!this.filtered().length) this.map = null;
    setTimeout(() => this.renderMap());
  }
  choose(route: PollTemplate): void { this.selectedId.set(route.templateId); this.renderMap(); }
  onMapReady(map: L.Map): void { this.map = map; this.renderMap(); }
  renderMap(): void {
    if (!this.map) return;
    this.layers.forEach(layer => layer.remove()); this.layers = [];
    const points: L.LatLngExpression[] = [];
    this.selected()?.defaultOptions.forEach((stop, index) => {
      if (stop.latitude == null || stop.longitude == null || !Number.isFinite(stop.latitude) || !Number.isFinite(stop.longitude) || Math.abs(stop.latitude) > 90 || Math.abs(stop.longitude) > 180) return;
      const point: L.LatLngExpression = [stop.latitude, stop.longitude]; points.push(point);
      const text = document.createElement('span'); text.textContent = `${index + 1}. ${stop.stopName}`;
      const marker = L.circleMarker(point, { radius: 8, color: '#fff', weight: 2, fillColor: '#003b35', fillOpacity: 1 }).bindPopup(text).addTo(this.map!);
      this.layers.push(marker);
    });
    if (points.length > 1) this.layers.push(L.polyline(points, { color: '#16a34a', weight: 4 }).addTo(this.map));
    if (points.length === 1) this.map.setView(points[0], 14);
    else if (points.length > 1) this.map.fitBounds(L.latLngBounds(points), { padding: [30, 30], maxZoom: 14 });
    else this.map.setView([-7.12, -36.72], 8);
    this.map.invalidateSize();
  }
  focusStop(stop: PollOptionResponse): void { if (this.map && stop.latitude != null && stop.longitude != null) this.map.setView([stop.latitude, stop.longitude], 15); }
  shiftLabel(value: string): string {
    const v = this.normalize(value); return v === 'manha' || v === 'morning' ? 'Manhã' : v === 'tarde' || v === 'afternoon' ? 'Tarde' : v === 'noite' || v === 'night' ? 'Noite' : value;
  }
  open(route: PollTemplate | null): void {
    this.formRequest?.unsubscribe();
    this.editing.set(route); this.error.set(''); this.formDataError.set(false);
    this.formLoading.set(true); this.options.set([]); this.universities.set([]);
    this.form.reset({ routeName: route?.routeName ?? '', shift: route ? this.shiftValue(route.shift) : '', start: route?.defaultStartTime?.slice(0, 5) ?? '', end: route?.defaultEndTime?.slice(0, 5) ?? '' });
    this.selectedStops.set(route?.defaultOptions.map(item => item.optionId) ?? []);
    this.selectedUniversities.set(route?.targetUniversities.map(item => item.id) ?? []);
    this.dialog.nativeElement.showModal();
    this.formRequest = forkJoin({ stops: this.optionsService.getAllPollOptions(), universities: this.universityService.getAllUniversityByNameFromState('', 'Paraíba') }).pipe(takeUntilDestroyed(this.destroyRef), finalize(() => this.formLoading.set(false))).subscribe({
      next: data => {
        this.options.set(data.stops);
        const existing = route?.targetUniversities ?? [];
        this.universities.set([...data.universities, ...existing.filter(item => !data.universities.some(candidate => candidate.id === item.id))]);
      },
      error: () => this.formDataError.set(true)
    });
  }
  private shiftValue(value: string): string { const label = this.shiftLabel(value); return label === 'Manhã' ? 'MANHA' : label === 'Tarde' ? 'TARDE' : label === 'Noite' ? 'NOITE' : value; }
  toggleUniversity(id: string, checked: boolean): void { this.selectedUniversities.update(ids => checked ? [...ids, id] : ids.filter(value => value !== id)); }
  addStop(id: string): void { if (id && !this.selectedStops().includes(id)) this.selectedStops.update(ids => [...ids, id]); }
  stopName(id: string): string { return this.options().find(item => item.optionId === id)?.stopName ?? this.editing()?.defaultOptions.find(item => item.optionId === id)?.stopName ?? 'Parada indisponível'; }
  moveStop(index: number, offset: number): void { this.selectedStops.update(ids => { const result = [...ids]; const other = index + offset; if (other < 0 || other >= ids.length) return ids; [result[index], result[other]] = [result[other], result[index]]; return result; }); }
  removeStop(id: string): void { this.selectedStops.update(ids => ids.filter(item => item !== id)); }
  close(): void { if (!this.saving()) { this.formRequest?.unsubscribe(); this.dialog.nativeElement.close(); } }
  save(): void {
    if (this.saving() || this.formDataError() || this.formLoading()) return;
    this.form.markAllAsTouched(); const value = this.form.getRawValue();
    if (this.form.invalid || !this.selectedStops().length || !this.selectedUniversities().length || this.selectedUniversities().length > 5 || value.start === value.end) {
      this.error.set('Preencha os campos, escolha de 1 a 5 universidades e ao menos uma parada. Os horários devem ser diferentes.'); return;
    }
    const payload = { routeName: value.routeName.trim(), shift: value.shift as Shift, defaultStartTime: value.start, defaultEndTime: value.end, targetUniversityIds: this.selectedUniversities(), defaultOptionIds: this.selectedStops() };
    const existing = this.editing(); const request = existing ? this.service.updatePollTemplate(existing.templateId, payload) : this.service.createPollTemplate(payload);
    this.saving.set(true); this.error.set('');
    request.pipe(takeUntilDestroyed(this.destroyRef), finalize(() => this.saving.set(false))).subscribe({
      next: route => { this.routes.update(items => existing ? items.map(item => item.templateId === route.templateId ? route : item) : [...items, route]); this.selectedId.set(route.templateId); this.notice.set(existing ? 'Rota atualizada.' : 'Rota cadastrada.'); this.dialog.nativeElement.close(); setTimeout(() => this.renderMap()); },
      error: (err: HttpErrorResponse) => this.error.set(err.error?.message || 'Não foi possível salvar a rota. Tente novamente.')
    });
  }
}
