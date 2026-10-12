import { ChangeDetectionStrategy, Component, computed, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { LeafletModule } from '@bluehalo/ngx-leaflet';
import * as L from 'leaflet';
import { AuthService } from '../../../../core/auth/services/auth.service';
import { PollService } from '../../../polls/services/poll.service';
import { PollList, Shift } from '../../../../core/models';
import { BusCardComponent, BusCardItem } from '../bus-card/bus-card.component';
import { SidebarComponent } from '../../../../shared/components/sidebar/sidebar.component';

@Component({
  selector: 'app-student-home',
  imports: [CommonModule, LeafletModule, BusCardComponent, SidebarComponent],
  templateUrl: './student-home.component.html',
  styleUrl: './student-home.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class StudentHomeComponent implements OnInit {
  public readonly authService = inject(AuthService);
  private readonly pollService = inject(PollService);
  private readonly router = inject(Router);

  public readonly isMobileMenuOpen = signal<boolean>(false);
  public readonly activeTab = signal<'inicio' | 'rotas' | 'onibus' | 'enquetes' | 'perfil'>('inicio');

  public readonly isLoading = signal<boolean>(true);
  public readonly pollsList = signal<PollList[]>([]);
  public readonly selectedBusId = signal<string | null>(null);

  public readonly studentName = computed(() => {
    const user = this.authService.currentUser();
    if (!user) return 'Estudante';
    const rawName = user.name || user.email.split('@')[0];
    return rawName.split(' ')[0];
  });

  public readonly busItems = computed<BusCardItem[]>(() => {
    const polls = this.pollsList();
    if (polls.length === 0) {
      return [];
    }

    return polls.map((poll) => {
      const shiftLabel =
        poll.shift === Shift.MORNING ? 'Manhã' : poll.shift === Shift.AFTERNOON ? 'Tarde' : 'Noite';

      const formattedStartTime = poll.startTime ? (poll.startTime.length >= 16 ? poll.startTime.substring(11, 16) : poll.startTime) : '--:--';
      const formattedEndTime = poll.endTime ? (poll.endTime.length >= 16 ? poll.endTime.substring(11, 16) : poll.endTime) : '--:--';

      return {
        id: poll.pollId,
        routeName: poll.routeName,
        shift: shiftLabel,
        pollDate: poll.pollDate,
        startTime: formattedStartTime,
        endTime: formattedEndTime,
        confirmedVotes: poll.confirmedVotesCount || 0
      };
    });
  });

  public readonly selectedBus = computed(() => {
    const items = this.busItems();
    const id = this.selectedBusId();
    if (!id) return items[0] || null;
    return items.find((item) => item.id === id) || items[0] || null;
  });

  public readonly availableRoutesCount = computed(() => {
    return this.busItems().length;
  });

  public mapOptions: L.MapOptions = {
    layers: [
      L.tileLayer('https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png', {
        maxZoom: 19,
        attribution: '&copy; OpenStreetMap contributors &copy; CARTO'
      })
    ],
    zoom: 13,
    center: L.latLng(-6.8554, -35.4908),
    zoomControl: false
  };

  public mapLayers: L.Layer[] = [];
  private leafletMap: L.Map | null = null;

  ngOnInit(): void {
    this.configureLeafletDefaults();
    this.loadOpenPolls();
  }

  onMapReady(map: L.Map): void {
    this.leafletMap = map;
    L.control.zoom({ position: 'bottomright' }).addTo(map);
    setTimeout(() => {
      map.invalidateSize();
      this.renderRouteElements();
    }, 250);
  }

  private configureLeafletDefaults(): void {
    const defaultIcon = L.icon({
      iconUrl: 'leaflet/marker-icon.png',
      iconRetinaUrl: 'leaflet/marker-icon-2x.png',
      shadowUrl: 'leaflet/marker-shadow.png',
      iconSize: [25, 41],
      iconAnchor: [12, 41],
      popupAnchor: [1, -34],
      tooltipAnchor: [16, -28],
      shadowSize: [41, 41]
    });
    L.Marker.prototype.options.icon = defaultIcon;
  }

  private loadOpenPolls(): void {
    this.isLoading.set(true);
    this.pollService.getOpenPollsToStudent().subscribe({
      next: (polls) => {
        this.pollsList.set(polls);
        if (polls.length > 0) {
          this.selectedBusId.set(polls[0].pollId);
        } else {
          this.selectedBusId.set(null);
        }
        this.isLoading.set(false);
        this.renderRouteElements();
      },
      error: () => {
        this.pollsList.set([]);
        this.selectedBusId.set(null);
        this.isLoading.set(false);
        this.renderRouteElements();
      }
    });
  }

  public selectBusCard(item: BusCardItem): void {
    this.selectedBusId.set(item.id);
    this.renderRouteElements();
  }

  private renderRouteElements(): void {
    const selected = this.selectedBus();
    if (!selected) {
      this.mapLayers = [];
      return;
    }

    const busCurrentLocation: [number, number] = [-6.8600, -35.4870];

    const busIcon = L.divIcon({
      className: 'custom-bus-marker',
      html: `
        <div style="background-color: #16A34A; width: 36px; height: 36px; border: 2.5px solid white; border-radius: 50%; box-shadow: 0 4px 12px rgba(22,163,74,0.45); display: flex; align-items: center; justify-content: center; color: white;">
          <svg style="width: 20px; height: 20px;" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M8 6v6" />
            <path d="M15 6v6" />
            <path d="M2 12h19.6" />
            <path d="M18 18h3s.5-1.7.8-2.8c.1-.4.2-.8.2-1.2 0-.6-.5-1-1-1H3c-.5 0-1 .4-1 1 0 .4.1.8.2 1.2.3 1.1.8 2.8.8 2.8h3" />
            <circle cx="7" cy="18" r="2" />
            <path d="M9 18h5" />
            <circle cx="16" cy="18" r="2" />
          </svg>
        </div>
      `,
      iconSize: [36, 36],
      iconAnchor: [18, 18]
    });

    const busMarker = L.marker(busCurrentLocation, { icon: busIcon }).bindPopup(
      `<div style="font-family: Inter, sans-serif; padding: 4px;">
        <strong style="color: #16A34A; font-size: 13px;">${selected.routeName}</strong><br>
        <span style="color: #64748B; font-size: 11px;">Turno: ${selected.shift} &bull; ${selected.confirmedVotes} confirmados</span>
      </div>`
    );

    this.mapLayers = [busMarker];

    if (this.leafletMap) {
      this.leafletMap.setView(busCurrentLocation, 14);
    }
  }

  public toggleMobileMenu(): void {
    this.isMobileMenuOpen.update((open) => !open);
  }

  public closeMobileMenu(): void {
    this.isMobileMenuOpen.set(false);
  }

  public setActiveTab(tab: 'inicio' | 'rotas' | 'onibus' | 'enquetes' | 'perfil'): void {
    this.activeTab.set(tab);
    this.closeMobileMenu();
    if (tab === 'enquetes') {
      this.router.navigate(['/student/polls']);
    } else if (tab === 'perfil') {
      this.router.navigate(['/student/profile']);
    }
  }

  public logout(): void {
    this.authService.logout();
  }
}
