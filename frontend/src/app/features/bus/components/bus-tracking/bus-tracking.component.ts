import { ChangeDetectionStrategy, Component, computed, inject, OnDestroy, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { LeafletModule } from '@bluehalo/ngx-leaflet';
import * as L from 'leaflet';
import { Subscription } from 'rxjs';
import { AuthService } from '../../../../core/auth/services/auth.service';
import { PollService } from '../../../polls/services/poll.service';
import { BusService } from '../../services/bus.service';
import { BusTrackingWebSocketService } from '../../services/bus-tracking-websocket.service';
import { BusLocation, Poll, PollList, PollOptionResponse, Shift } from '../../../../core/models';
import { SidebarComponent } from '../../../../shared/components/sidebar/sidebar.component';

@Component({
  selector: 'app-bus-tracking',
  imports: [CommonModule, RouterModule, LeafletModule, SidebarComponent],
  templateUrl: './bus-tracking.component.html',
  styleUrl: './bus-tracking.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class BusTrackingComponent implements OnInit, OnDestroy {
  public readonly authService = inject(AuthService);
  private readonly pollService = inject(PollService);
  private readonly busService = inject(BusService);
  public readonly wsService = inject(BusTrackingWebSocketService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  public readonly isMobileMenuOpen = signal<boolean>(false);
  public readonly isMobileSheetExpanded = signal<boolean>(false);
  public readonly isLoading = signal<boolean>(true);
  public readonly openPolls = signal<PollList[]>([]);
  public readonly selectedPoll = signal<Poll | null>(null);
  public readonly selectedPollId = signal<string | null>(null);

  public readonly currentBusLocation = signal<BusLocation | null>(null);
  public readonly lastUpdatedTime = signal<string | null>(null);
  public readonly isAutoCenter = signal<boolean>(true);

  private lastLocationTimestamp = signal<number>(0);
  private inactivityCheckTimer: any = null;

  public readonly isBusInTransit = computed<boolean>(() => {
    return this.currentBusLocation() !== null && this.openPolls().length > 0;
  });

  private wsSubscription: Subscription | null = null;
  private leafletMap: L.Map | null = null;
  private busMarker: L.Marker | null = null;
  private stopMarkers: L.Marker[] = [];

  public mapOptions: L.MapOptions = {
    layers: [
      L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        attribution: '&copy; OpenStreetMap contributors'
      })
    ],
    zoom: 14,
    center: L.latLng(-6.8885, -35.1258),
    zoomControl: false
  };

  public readonly stopsList = computed<PollOptionResponse[]>(() => {
    const poll = this.selectedPoll();
    if (!poll || !poll.options) return [];
    return poll.options;
  });

  public readonly formattedShift = computed(() => {
    const poll = this.selectedPoll();
    if (!poll) return '';
    return poll.shift === Shift.MORNING ? 'Manhã' : poll.shift === Shift.AFTERNOON ? 'Tarde' : 'Noite';
  });

  ngOnInit(): void {
    this.configureLeafletDefaults();
    this.wsService.connect();
    this.loadInitialData();

    // Verifica periodicamente se o ônibus parou de enviar sinal (inatividade > 35s)
    this.inactivityCheckTimer = setInterval(() => {
      const last = this.lastLocationTimestamp();
      if (last > 0 && Date.now() - last > 35000) {
        if (this.currentBusLocation() !== null) {
          this.currentBusLocation.set(null);
          if (this.busMarker) {
            this.busMarker.remove();
            this.busMarker = null;
          }
        }
      }
    }, 10000);
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

  public onMapReady(map: L.Map): void {
    this.leafletMap = map;
    L.control.zoom({ position: 'bottomright' }).addTo(map);

    setTimeout(() => {
      map.invalidateSize();
      this.refreshMapElements();
    }, 250);
  }

  public loadInitialData(): void {
    this.isLoading.set(true);
    this.pollService.getOpenPollsToStudent().subscribe({
      next: (polls) => {
        this.openPolls.set(polls);
        if (polls.length > 0) {
          const paramPollId = this.route.snapshot.queryParamMap.get('pollId');
          const chosenId = paramPollId && polls.some((p) => p.pollId === paramPollId) ? paramPollId : polls[0].pollId;
          this.selectPoll(chosenId);
          this.checkCachedBusLocation();
        } else {
          this.isLoading.set(false);
        }
      },
      error: () => {
        this.openPolls.set([]);
        this.isLoading.set(false);
      }
    });
  }

  private checkCachedBusLocation(): void {
    this.busService.getLatestLocation('f0000000-0000-0000-0000-000000000001').subscribe({
      next: (loc) => {
        if (loc && loc.latitude != null && loc.longitude != null) {
          const time = loc.timestamp ? new Date(loc.timestamp).getTime() : 0;
          if (time > 0 && Date.now() - time < 35000) {
            this.onNewBusLocation(loc);
          }
        }
      },
      error: () => {
        // Sem coordenadas recentes em cache
      }
    });
  }

  public selectPoll(pollId: string): void {
    this.selectedPollId.set(pollId);
    this.isLoading.set(true);

    this.pollService.getPoll(pollId).subscribe({
      next: (poll) => {
        this.selectedPoll.set(poll);
        this.isLoading.set(false);
        this.refreshMapElements();
        this.startTrackingForPoll(poll);
      },
      error: () => {
        this.isLoading.set(false);
      }
    });
  }

  private startTrackingForPoll(poll: Poll): void {
    if (this.wsSubscription) {
      this.wsSubscription.unsubscribe();
      this.wsSubscription = null;
    }

    // Inscrição no canal WebSocket STOMP geral de atualizações ao vivo
    this.wsSubscription = this.wsService.listenAllLocations().subscribe({
      next: (loc) => {
        this.onNewBusLocation(loc);
      }
    });
  }

  private onNewBusLocation(loc: BusLocation): void {
    const wasInTransit = this.isBusInTransit();
    this.currentBusLocation.set(loc);
    this.lastLocationTimestamp.set(Date.now());
    this.lastUpdatedTime.set(this.formatTime(new Date()));
    this.updateBusMarkerOnMap(loc.latitude, loc.longitude);

    if (!wasInTransit && this.leafletMap) {
      setTimeout(() => {
        this.leafletMap?.invalidateSize();
        this.refreshMapElements();
      }, 150);
    }

    if (this.isAutoCenter() && this.leafletMap) {
      this.leafletMap.panTo([loc.latitude, loc.longitude]);
    }
  }

  private updateBusMarkerOnMap(lat: number, lng: number): void {
    if (!this.leafletMap) return;

    const busIcon = L.divIcon({
      className: 'bus-pulse-marker',
      html: `
        <div style="position: relative; width: 44px; height: 44px; display: flex; align-items: center; justify-content: center;">
          <div style="position: absolute; width: 100%; height: 100%; border-radius: 50%; background: rgba(22, 163, 74, 0.35); animation: bus-radar-pulse 2s infinite ease-out;"></div>
          <div style="width: 34px; height: 34px; border-radius: 50%; background: #16A34A; border: 2.5px solid white; box-shadow: 0 4px 10px rgba(0,0,0,0.25); display: flex; align-items: center; justify-content: center; color: white;">
            <svg style="width: 18px; height: 18px;" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.3" stroke-linecap="round" stroke-linejoin="round">
              <path d="M8 6v6" />
              <path d="M15 6v6" />
              <path d="M2 12h19.6" />
              <path d="M18 18h3s.5-1.7.8-2.8c.1-.4.2-.8.2-1.2 0-.6-.5-1-1-1H3c-.5 0-1 .4-1 1 0 .4.1.8.2 1.2.3 1.1.8 2.8.8 2.8h3" />
              <circle cx="7" cy="18" r="2" />
              <path d="M9 18h5" />
              <circle cx="16" cy="18" r="2" />
            </svg>
          </div>
        </div>
      `,
      iconSize: [44, 44],
      iconAnchor: [22, 22]
    });

    if (this.busMarker) {
      this.busMarker.setLatLng([lat, lng]);
    } else {
      this.busMarker = L.marker([lat, lng], { icon: busIcon, zIndexOffset: 1000 }).addTo(this.leafletMap);
      this.busMarker.bindPopup(`
        <div style="font-family: Inter, sans-serif; padding: 6px; font-size: 13px;">
          <strong style="color: #16A34A;">Ônibus Universitário em trânsito</strong><br>
          <span style="color: #64748B; font-size: 11px;">Rastreamento em tempo real</span>
        </div>
      `);
    }
  }

  private refreshMapElements(): void {
    if (!this.leafletMap) return;

    // Limpa marcadores anteriores de pontos
    this.stopMarkers.forEach((m) => m.remove());
    this.stopMarkers = [];

    const poll = this.selectedPoll();
    if (!poll || !poll.options) return;

    const latLngs: [number, number][] = [];

    // Renderiza cada ponto de parada no mapa com número de ordem
    poll.options.forEach((opt, index) => {
      if (opt.latitude != null && opt.longitude != null) {
        const pointCoords: [number, number] = [opt.latitude, opt.longitude];
        latLngs.push(pointCoords);

        const stopIcon = L.divIcon({
          className: 'stop-point-marker',
          html: `
            <div style="width: 26px; height: 26px; border-radius: 50%; background: #003B35; color: white; border: 2px solid white; box-shadow: 0 2px 6px rgba(0,0,0,0.3); display: flex; align-items: center; justify-content: center; font-size: 11px; font-weight: 700; font-family: Inter, sans-serif;">
              ${index + 1}
            </div>
          `,
          iconSize: [26, 26],
          iconAnchor: [13, 13]
        });

        const marker = L.marker(pointCoords, { icon: stopIcon })
          .addTo(this.leafletMap!)
          .bindPopup(`
            <div style="font-family: Inter, sans-serif; padding: 4px; font-size: 12px;">
              <span style="font-size: 10px; font-weight: 700; color: #16A34A; text-transform: uppercase;">Parada #${index + 1}</span><br>
              <strong style="color: #0F172A; font-size: 13px;">${opt.stopName}</strong>
            </div>
          `);

        this.stopMarkers.push(marker);
      }
    });

    // Enquadra o mapa para exibir todas as paradas
    if (latLngs.length > 1) {
      this.leafletMap.fitBounds(L.latLngBounds(latLngs), { padding: [50, 50] });
    } else if (latLngs.length === 1) {
      this.leafletMap.setView(latLngs[0], 14);
    }
  }

  public toggleAutoCenter(): void {
    this.isAutoCenter.update((v) => !v);
    if (this.isAutoCenter() && this.currentBusLocation() && this.leafletMap) {
      const loc = this.currentBusLocation()!;
      this.leafletMap.panTo([loc.latitude, loc.longitude]);
    }
  }

  public focusStop(opt: PollOptionResponse): void {
    if (opt.latitude != null && opt.longitude != null && this.leafletMap) {
      this.isAutoCenter.set(false);
      this.leafletMap.flyTo([opt.latitude, opt.longitude], 16, { duration: 1 });
      if (this.isMobileSheetExpanded()) {
        this.isMobileSheetExpanded.set(false);
      }
    }
  }

  public toggleMobileSheet(): void {
    this.isMobileSheetExpanded.update((v) => !v);
  }

  public toggleMobileMenu(): void {
    this.isMobileMenuOpen.update((open) => !open);
  }

  public closeMobileMenu(): void {
    this.isMobileMenuOpen.set(false);
  }

  public setActiveTab(tab: 'inicio' | 'rotas' | 'onibus' | 'enquetes' | 'perfil'): void {
    if (tab === 'inicio' || tab === 'rotas' || tab === 'onibus') {
      this.router.navigate(['/student']);
    } else if (tab === 'enquetes') {
      this.router.navigate(['/student/polls']);
    } else if (tab === 'perfil') {
      this.router.navigate(['/student/profile']);
    }
  }

  public logout(): void {
    this.authService.logout();
  }

  private formatTime(date: Date): string {
    return date.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit', second: '2-digit' });
  }

  ngOnDestroy(): void {
    if (this.inactivityCheckTimer) {
      clearInterval(this.inactivityCheckTimer);
      this.inactivityCheckTimer = null;
    }
    if (this.wsSubscription) {
      this.wsSubscription.unsubscribe();
      this.wsSubscription = null;
    }
  }
}

