import { ChangeDetectionStrategy, Component, computed, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { LeafletModule } from '@bluehalo/ngx-leaflet';
import * as L from 'leaflet';
import { catchError, forkJoin, of } from 'rxjs';
import { AuthService } from '../../../../core/auth/services/auth.service';
import { Bus, BusLocation, PollTemplate, Travel } from '../../../../core/models';
import { BusService } from '../../../bus/services/bus.service';
import { PollTemplateService } from '../../../poll-templates/services/poll-template.service';
import { TravelService } from '../../../traveling/services/travel.service';

@Component({
  selector: 'app-municipality-dashboard',
  standalone: true,
  imports: [CommonModule, LeafletModule],
  templateUrl: './municipality-dashboard.component.html',
  styleUrl: './municipality-dashboard.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class MunicipalityDashboardComponent implements OnInit {
  readonly auth = inject(AuthService);
  private readonly busesService = inject(BusService);
  private readonly templatesService = inject(PollTemplateService);
  private readonly travelsService = inject(TravelService);
  private readonly destroyRef = inject(DestroyRef);
  readonly isLoading = signal(true);
  readonly menuOpen = signal(false);
  readonly buses = signal<Bus[] | null>(null);
  readonly templates = signal<PollTemplate[] | null>(null);
  readonly travels = signal<Travel[] | null>(null);
  readonly locations = signal<Record<string, BusLocation> | null>(null);
  readonly municipalityName = computed(() => {
    const user = this.auth.currentUser();
    const name = user?.name?.trim();
    // Tokens antigos podem não conter o nome da prefeitura.
    return name && name !== user?.email && name !== user?.email?.split('@')[0] ? name : null;
  });
  readonly municipalityLabel = computed(() => {
    const name = this.municipalityName();
    if (!name) return 'Prefeitura';
    return /^prefeitura\b/i.test(name) ? name : `Prefeitura de ${name}`;
  });
  readonly activeTravels = computed(() => this.travels()?.filter(t => this.isInProgress(t.status)) ?? []);
  readonly activeBusCount = computed(() => this.travels() === null ? null : new Set(this.activeTravels().map(t => t.bus?.busId).filter(Boolean)).size);
  readonly recentTravels = computed(() => this.travels()?.slice(0, 5) ?? []);
  readonly visibleLocations = computed(() => {
    const buses = this.buses();
    const positions = this.locations();
    if (!buses || !positions) return [];
    return buses.flatMap(bus => {
      const position = positions[bus.busId];
      if (!position || !Number.isFinite(position.latitude) || !Number.isFinite(position.longitude) || Math.abs(position.latitude) > 90 || Math.abs(position.longitude) > 180) return [];
      return [{ bus, position }];
    });
  });
  readonly hasErrors = computed(() => !this.isLoading() && [this.buses(), this.templates(), this.travels(), this.locations()].some(value => value === null));
  readonly mapOptions: L.MapOptions = {
    layers: [L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', { maxZoom: 19, attribution: '&copy; OpenStreetMap contributors' })],
    center: L.latLng(-7.12, -36.72), zoom: 8, zoomControl: false
  };
  private map: L.Map | null = null;
  private markers: L.Layer[] = [];

  ngOnInit(): void { this.loadDashboard(); }

  loadDashboard(): void {
    this.isLoading.set(true);
    this.map = null;
    this.markers = [];
    forkJoin({
      buses: this.busesService.getAllBusesByMunicipality().pipe(catchError(() => of(null))),
      templates: this.templatesService.getAllPollTemplates().pipe(catchError(() => of(null))),
      travels: this.travelsService.getAllTravelsByMunicipality().pipe(catchError(() => of(null))),
      locations: this.busesService.getMunicipalityLocations().pipe(catchError(() => of(null)))
    }).pipe(takeUntilDestroyed(this.destroyRef)).subscribe(result => {
      this.buses.set(result.buses);
      this.templates.set(result.templates);
      this.travels.set(result.travels);
      this.locations.set(result.locations);
      this.isLoading.set(false);
    });
  }

  isInProgress(status: string | null | undefined): boolean {
    const value = status?.normalize('NFD').replace(/[\u0300-\u036f]/g, '').trim().replace(/[\s-]+/g, '_').toUpperCase();
    return value === 'IN_PROGRESS' || value === 'EM_ANDAMENTO';
  }

  activeTravelFor(busId: string): Travel | undefined {
    return this.activeTravels().find(travel => travel.bus?.busId === busId);
  }

  onMapReady(map: L.Map): void {
    this.map = map;
    L.control.zoom({ position: 'bottomright' }).addTo(map);
    this.renderMarkers();
  }

  private renderMarkers(): void {
    if (!this.map) return;
    this.markers.forEach(marker => marker.remove());
    this.markers = [];
    const points: L.LatLngExpression[] = [];
    for (const { bus, position } of this.visibleLocations()) {
      const latLng: L.LatLngExpression = [position.latitude, position.longitude];
      points.push(latLng);
      const popup = document.createElement('div');
      const title = document.createElement('strong');
      title.textContent = bus.busName;
      const time = document.createElement('p');
      time.textContent = position.timestamp ? `Última posição: ${new Date(position.timestamp).toLocaleString('pt-BR')}` : 'Horário da posição não informado';
      popup.append(title, time);
      const marker = L.circleMarker(latLng, { radius: 8, color: '#ffffff', weight: 2, fillColor: '#16a34a', fillOpacity: 1 }).bindPopup(popup).addTo(this.map);
      this.markers.push(marker);
    }
    if (points.length === 1) this.map.setView(points[0], 14);
    else if (points.length > 1) this.map.fitBounds(L.latLngBounds(points), { padding: [28, 28], maxZoom: 14 });
    this.map.invalidateSize();
  }
}
