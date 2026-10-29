import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  OnDestroy,
  OnInit,
  signal
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { LeafletModule } from '@bluehalo/ngx-leaflet';
import * as L from 'leaflet';
import { Subscription } from 'rxjs';
import { AuthService } from '../../../../core/auth/services/auth.service';
import { TravelService } from '../../../traveling/services/travel.service';
import { PollService } from '../../../polls/services/poll.service';
import { BusService } from '../../../bus/services/bus.service';
import { BusTrackingWebSocketService } from '../../../bus/services/bus-tracking-websocket.service';
import {
  Bus,
  BusLocation,
  isOutboundTravel,
  Poll,
  PollOptionResponse,
  Shift,
  Travel,
  TravelStatus,
  UniversityResponse
} from '../../../../core/models';
import { DriverHeaderComponent } from '../../../../shared/components/driver-header/driver-header.component';

@Component({
  selector: 'app-travel-in-progress',
  standalone: true,
  imports: [CommonModule, RouterModule, LeafletModule, DriverHeaderComponent],
  templateUrl: './travel-in-progress.component.html',
  styleUrl: './travel-in-progress.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class TravelInProgressComponent implements OnInit, OnDestroy {
  public readonly authService = inject(AuthService);
  private readonly travelService = inject(TravelService);
  private readonly pollService = inject(PollService);
  private readonly busService = inject(BusService);
  private readonly wsService = inject(BusTrackingWebSocketService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  public readonly isLoading = signal<boolean>(true);
  public readonly hasError = signal<boolean>(false);
  public readonly errorMessage = signal<string | null>(null);

  public readonly travel = signal<Travel | null>(null);
  public readonly poll = signal<Poll | null>(null);
  public readonly bus = signal<Bus | null>(null);

  public readonly busLocation = signal<BusLocation | null>(null);
  public readonly lastSignalTime = signal<string | null>(null);

  public readonly showConfirmModal = signal<boolean>(false);
  public readonly isCompleting = signal<boolean>(false);

  public readonly driverName = computed<string>(() => {
    const user = this.authService.currentUser();
    if (user?.name) return user.name;
    if (user?.email) return user.email.split('@')[0];
    return this.travel()?.busDriver?.busDriverName || 'Motorista';
  });

  public readonly driverEmail = computed<string>(() => {
    return this.authService.currentUser()?.email || '';
  });

  private leafletMap: L.Map | null = null;
  private routePolyline: L.Polyline | null = null;
  private stopMarkers: L.Marker[] = [];
  private busMarker: L.Marker | null = null;
  private wsSubscription: Subscription | null = null;
  private geoWatchId: number | null = null;

  public mapOptions: L.MapOptions = {
    layers: [
      L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        attribution: '&copy; OpenStreetMap contributors'
      })
    ],
    zoom: 15,
    center: L.latLng(-6.8375, -35.1278),
    zoomControl: false
  };

  public readonly stops = computed<PollOptionResponse[]>(() => {
    return this.poll()?.options || [];
  });

  public readonly firstStop = computed<PollOptionResponse | null>(() => {
    const list = this.stops();
    return list.length > 0 ? list[0] : null;
  });

  public readonly routeName = computed<string>(() => {
    return this.poll()?.routeName || 'Rota da Viagem';
  });

  public readonly shiftFormatted = computed<string>(() => {
    const s = this.poll()?.shift || this.travel()?.shift;
    if (s === Shift.MORNING) return 'Manhã';
    if (s === Shift.AFTERNOON) return 'Tarde';
    if (s === Shift.NIGHT) return 'Noite';
    return '';
  });

  public readonly isOutbound = computed<boolean>(() => {
    return isOutboundTravel(this.travel()?.direction);
  });

  public readonly formattedDate = computed<string>(() => {
    const raw = this.travel()?.travelDate || this.poll()?.pollDate;
    if (!raw) return '';
    try {
      const parts = raw.split('-');
      if (parts.length === 3) {
        return `${parts[2]}/${parts[1]}/${parts[0]}`;
      }
      return new Date(raw).toLocaleDateString('pt-BR');
    } catch {
      return raw;
    }
  });

  public readonly departureTimeFormatted = computed<string>(() => {
    const raw = this.travel()?.departureTime || this.poll()?.startTime;
    if (!raw) return '--:--';
    return this.formatTimeOnly(raw);
  });

  public readonly returnTimeFormatted = computed<string>(() => {
    const raw = this.travel()?.returnTime;
    if (!raw) return '--:--';
    return this.formatTimeOnly(raw);
  });

  public readonly totalVotes = computed<number>(() => {
    return this.poll()?.confirmedVotesCount ?? this.travel()?.confirmedVotesCount ?? 0;
  });

  public readonly universities = computed<UniversityResponse[]>(() => {
    return this.poll()?.targetUniversities || [];
  });

  public readonly maxUniVotes = computed<number>(() => {
    const counts = this.universities().map((u) => u.voteCount || 0);
    return Math.max(1, ...counts);
  });

  public readonly maxStopVotes = computed<number>(() => {
    const counts = this.stops().map((s) => s.voteCount || 0);
    return Math.max(1, ...counts);
  });

  public trackByUniId(index: number, uni: UniversityResponse): string {
    return uni.id;
  }

  public trackByStopId(index: number, stop: PollOptionResponse): string {
    return stop.optionId;
  }

  ngOnInit(): void {
    this.configureLeafletDefaults();
    this.loadData();
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
      const loc = this.busLocation();
      if (loc && loc.latitude != null && loc.longitude != null) {
        this.updateBusMarkerOnMap(loc.latitude, loc.longitude, true);
      } else {
        this.ensureInitialBusMarker();
      }
    }, 250);
  }

  public loadData(): void {
    this.isLoading.set(true);
    this.hasError.set(false);
    this.errorMessage.set(null);

    // 1. Tenta recuperar viagem ativa do backend
    this.travelService.getActiveTravelByDriver().subscribe({
      next: (travelData) => {
        this.travel.set(travelData);
        if (travelData.bus) {
          this.bus.set(travelData.bus);
        }

        const pollId = travelData.pollId || (history.state?.poll as Poll)?.pollId;
        if (pollId) {
          this.loadPollDetails(pollId);
        } else {
          // Se veio do estado de navegação com a enquete completa
          const navPoll = (history.state?.poll as Poll) || null;
          if (navPoll) {
            this.poll.set(navPoll);
            this.isLoading.set(false);
            this.refreshMapElements();
            this.ensureInitialBusMarker();
          } else {
            this.isLoading.set(false);
          }
        }

        // Tenta consultar última localização do ônibus se houver veículo
        if (travelData.bus?.busId) {
          this.initBusTracking(travelData.bus.busId);
        }
      },
      error: (err) => {
        // Fallback: se houver state de navegação após startTravel
        const navTravel = history.state?.travel as Travel | undefined;
        const navPoll = history.state?.poll as Poll | undefined;
        if (navTravel) {
          this.travel.set(navTravel);
          if (navPoll) {
            this.poll.set(navPoll);
          }
          if (navTravel.bus) {
            this.bus.set(navTravel.bus);
          }
          this.isLoading.set(false);
          this.refreshMapElements();

          const busId = navTravel.bus?.busId;
          if (busId) {
            this.initBusTracking(busId);
          } else {
            this.ensureInitialBusMarker();
          }
        } else {
          console.error('Erro ao carregar viagem ativa:', err);
          this.hasError.set(true);
          this.errorMessage.set(
            err?.error?.message || 'Nenhuma viagem em andamento encontrada para este motorista.'
          );
          this.isLoading.set(false);
        }
      }
    });
  }

  private loadPollDetails(pollId: string): void {
    this.pollService.getPoll(pollId).subscribe({
      next: (pollData) => {
        this.poll.set(pollData);
        this.isLoading.set(false);
        setTimeout(() => {
          this.refreshMapElements();
          this.ensureInitialBusMarker();
        }, 150);
      },
      error: (err) => {
        console.error('Erro ao carregar detalhes da enquete:', err);
        const navPoll = (history.state?.poll as Poll) || null;
        if (navPoll) {
          this.poll.set(navPoll);
        }
        this.isLoading.set(false);
        setTimeout(() => {
          this.refreshMapElements();
          this.ensureInitialBusMarker();
        }, 150);
      }
    });
  }

  private initBusTracking(busId: string): void {
    // 1. Inicia escuta WebSocket STOMP em tempo real
    this.wsService.connect();
    if (this.wsSubscription) {
      this.wsSubscription.unsubscribe();
    }
    this.wsSubscription = this.wsService.listenBusLocation(busId).subscribe({
      next: (loc) => {
        if (loc && loc.latitude != null && loc.longitude != null) {
          this.busLocation.set(loc);
          this.lastSignalTime.set(
            new Date().toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })
          );
          this.updateBusMarkerOnMap(loc.latitude, loc.longitude, false);
        }
      }
    });

    // 2. Busca última localização conhecida via REST
    this.busService.getLatestLocation(busId).subscribe({
      next: (loc) => {
        if (loc && loc.latitude != null && loc.longitude != null) {
          this.busLocation.set(loc);
          if (loc.timestamp) {
            this.lastSignalTime.set(this.formatTimeOnly(loc.timestamp));
          }
          this.updateBusMarkerOnMap(loc.latitude, loc.longitude, true);
        } else {
          this.ensureInitialBusMarker();
        }
      },
      error: () => {
        this.ensureInitialBusMarker();
      }
    });

    // 3. Ativa transmissão de GPS do dispositivo do motorista
    this.startGeolocationTracking(busId);
  }

  private ensureInitialBusMarker(): void {
    if (this.busMarker || !this.leafletMap) return;

    const stopsList = this.stops();
    let initialCoords: [number, number] | null = null;

    if (stopsList.length > 0) {
      const targetStop = this.isOutbound() ? stopsList[0] : (stopsList[stopsList.length - 1] || stopsList[0]);
      if (targetStop.latitude != null && targetStop.longitude != null) {
        initialCoords = [targetStop.latitude, targetStop.longitude];
      }
    }

    if (!initialCoords) {
      initialCoords = [-6.8375, -35.1278];
    }

    this.updateBusMarkerOnMap(initialCoords[0], initialCoords[1], true);

    const busId = this.bus()?.busId || this.travel()?.bus?.busId;
    if (busId) {
      this.wsService.sendLocation(busId, initialCoords[0], initialCoords[1]);
      if (!this.lastSignalTime()) {
        this.lastSignalTime.set(
          new Date().toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })
        );
      }
    }
  }

  private startGeolocationTracking(busId: string): void {
    if (typeof navigator !== 'undefined' && 'geolocation' in navigator) {
      // Coleta imediata
      navigator.geolocation.getCurrentPosition(
        (pos) => {
          const lat = pos.coords.latitude;
          const lng = pos.coords.longitude;
          this.wsService.sendLocation(busId, lat, lng);
          this.updateBusMarkerOnMap(lat, lng, true);
          this.lastSignalTime.set(
            new Date().toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })
          );
        },
        (err) => {
          console.warn('GPS inicial não disponível via getCurrentPosition:', err);
          this.ensureInitialBusMarker();
        },
        { enableHighAccuracy: true, timeout: 8000 }
      );

      // Transmissão contínua
      if (this.geoWatchId !== null) {
        navigator.geolocation.clearWatch(this.geoWatchId);
      }

      this.geoWatchId = navigator.geolocation.watchPosition(
        (pos) => {
          const lat = pos.coords.latitude;
          const lng = pos.coords.longitude;
          this.wsService.sendLocation(busId, lat, lng);
          this.updateBusMarkerOnMap(lat, lng, false);
          this.lastSignalTime.set(
            new Date().toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })
          );
        },
        (err) => {
          console.warn('Aviso no monitoramento de GPS contínuo:', err);
        },
        { enableHighAccuracy: true, maximumAge: 4000, timeout: 10000 }
      );
    } else {
      this.ensureInitialBusMarker();
    }
  }

  private updateBusMarkerOnMap(lat: number, lng: number, autoOpenPopup: boolean = true): void {
    if (!this.leafletMap) return;

    const isOut = this.isOutbound();
    const primaryColor = isOut ? '#16A34A' : '#2563EB';
    const pulseAnim = isOut ? 'bus-radar-pulse 2s infinite ease-out' : 'bus-radar-pulse-blue 2s infinite ease-out';
    const pulseBg = isOut ? 'rgba(22, 163, 74, 0.35)' : 'rgba(37, 99, 235, 0.35)';

    const busIcon = L.divIcon({
      className: 'bus-pulse-marker',
      html: `
        <div style="position: relative; width: 44px; height: 44px; display: flex; align-items: center; justify-content: center;">
          <div style="position: absolute; width: 100%; height: 100%; border-radius: 50%; background: ${pulseBg}; animation: ${pulseAnim};"></div>
          <div style="width: 34px; height: 34px; border-radius: 50%; background: ${primaryColor}; border: 2.5px solid white; box-shadow: 0 4px 10px rgba(0,0,0,0.25); display: flex; align-items: center; justify-content: center; color: white;">
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

    const title = isOut ? 'Seu Ônibus (Viagem de Ida)' : 'Seu Ônibus (Viagem de Volta)';
    const sub = isOut ? 'A caminho das universidades' : 'Retornando ao município';
    const popupHtml = `
      <div style="font-family: Inter, sans-serif; padding: 6px; font-size: 13px; min-width: 160px;">
        <strong style="color: ${primaryColor}; font-size: 13px;">${title}</strong><br>
        <span style="color: #64748B; font-size: 11px;">${sub}</span>
      </div>
    `;

    if (this.busMarker) {
      this.busMarker.setLatLng([lat, lng]);
      this.busMarker.setIcon(busIcon);
      this.busMarker.setPopupContent(popupHtml);
      if (autoOpenPopup) {
        this.busMarker.openPopup();
      }
    } else {
      this.busMarker = L.marker([lat, lng], { icon: busIcon, zIndexOffset: 1000 }).addTo(this.leafletMap);
      this.busMarker.bindPopup(popupHtml);
      this.busMarker.openPopup();
    }

    this.focusMapOnRoute();
  }

  private refreshMapElements(): void {
    if (!this.leafletMap) return;

    // Limpa marcadores e linhas anteriores
    this.stopMarkers.forEach((m) => m.remove());
    this.stopMarkers = [];
    if (this.routePolyline) {
      this.routePolyline.remove();
      this.routePolyline = null;
    }

    const stopsList = this.stops();
    if (!stopsList || stopsList.length === 0) return;

    const isOut = this.isOutbound();
    const routeColor = isOut ? '#16A34A' : '#2563EB';
    const stopThemeColor = isOut ? '#003B35' : '#1E3A8A';
    const tagColor = isOut ? '#16A34A' : '#2563EB';
    const latLngs: [number, number][] = [];

    stopsList.forEach((opt, index) => {
      if (opt.latitude != null && opt.longitude != null) {
        const pointCoords: [number, number] = [opt.latitude, opt.longitude];
        latLngs.push(pointCoords);

        const stopIcon = L.divIcon({
          className: 'stop-point-marker',
          html: `
            <div style="width: 28px; height: 28px; border-radius: 50%; background: ${stopThemeColor}; color: white; border: 2.5px solid white; box-shadow: 0 2px 6px rgba(0,0,0,0.3); display: flex; align-items: center; justify-content: center; font-size: 11px; font-weight: 700; font-family: Inter, sans-serif;">
              ${index + 1}
            </div>
          `,
          iconSize: [28, 28],
          iconAnchor: [14, 14]
        });

        const studentText = isOut
          ? `${opt.voteCount ?? 0} aluno(s) aguardando`
          : `${opt.voteCount ?? 0} aluno(s) com desembarque previsto`;

        const marker = L.marker(pointCoords, { icon: stopIcon })
          .addTo(this.leafletMap!)
          .bindPopup(`
            <div style="font-family: Inter, sans-serif; padding: 4px; font-size: 12px;">
              <span style="font-size: 10px; font-weight: 700; color: ${tagColor}; text-transform: uppercase;">Parada #${index + 1}</span><br>
              <strong style="color: #0F172A; font-size: 13px;">${opt.stopName}</strong>
              <div style="color: #64748B; font-size: 11px; margin-top: 2px;">${studentText}</div>
            </div>
          `);

        this.stopMarkers.push(marker);
      }
    });

    // Traça linha conectando os pontos de parada em ordem
    if (latLngs.length > 1) {
      this.routePolyline = L.polyline(latLngs, {
        color: routeColor,
        weight: 4,
        opacity: 0.85,
        lineCap: 'round',
        lineJoin: 'round'
      }).addTo(this.leafletMap);
    }

    if (this.busMarker && !this.leafletMap.hasLayer(this.busMarker)) {
      this.busMarker.addTo(this.leafletMap);
      this.busMarker.openPopup();
    }

    this.focusMapOnRoute();
  }

  /**
   * Enquadra o mapa nas paradas. A posição do ônibus só entra no enquadramento
   * se estiver próxima da rota (<= 30 km); caso contrário, um sinal distante
   * tiraria o mapa de perto das paradas.
   */
  private focusMapOnRoute(): void {
    if (!this.leafletMap) return;

    const points: L.LatLng[] = this.stopMarkers.map((m) => m.getLatLng());
    if (points.length === 0) {
      if (this.busMarker) {
        this.leafletMap.setView(this.busMarker.getLatLng(), 15);
      } else {
        this.leafletMap.setView(L.latLng(-6.8375, -35.1278), 15);
      }
      return;
    }

    if (this.busMarker) {
      const busPos = this.busMarker.getLatLng();
      const center = L.latLngBounds(points).getCenter();
      if (center.distanceTo(busPos) <= 30000) {
        points.push(busPos);
      }
    }

    if (points.length === 1) {
      this.leafletMap.setView(points[0], 15);
    } else {
      this.leafletMap.fitBounds(L.latLngBounds(points), { padding: [40, 40], maxZoom: 16 });
    }
  }

  public openConfirmModal(): void {
    this.showConfirmModal.set(true);
  }

  public closeConfirmModal(): void {
    this.showConfirmModal.set(false);
  }

  public onCompleteTravel(): void {
    const travelId = this.travel()?.travelId;
    if (!travelId) return;

    this.isCompleting.set(true);
    const wasOutbound = this.isOutbound();

    this.travelService.completeTravel(travelId).subscribe({
      next: () => {
        this.isCompleting.set(false);
        this.showConfirmModal.set(false);
        this.router.navigate(['/driver'], {
          state: {
            travelCompleted: true,
            outboundCompleted: wasOutbound,
            notificationMessage: wasOutbound
              ? 'Viagem de ida concluída com sucesso! O retorno estará pronto para ser iniciado no final do dia.'
              : 'Viagem de volta concluída com sucesso! Todas as etapas de hoje foram finalizadas.'
          }
        });
      },
      error: (err) => {
        this.isCompleting.set(false);
        this.showConfirmModal.set(false);
        console.error('Erro ao finalizar viagem:', err);
        alert(err?.error?.message || 'Falha ao finalizar viagem. Tente novamente.');
      }
    });
  }

  public onBackToDashboard(): void {
    this.router.navigate(['/driver']);
  }

  public onLogout(): void {
    this.authService.logout();
  }

  private formatTimeOnly(raw: string): string {
    try {
      if (raw.includes('T')) {
        return raw.split('T')[1].substring(0, 5);
      }
      if (raw.includes(':')) {
        return raw.substring(0, 5);
      }
      return raw;
    } catch {
      return raw;
    }
  }

  ngOnDestroy(): void {
    if (this.geoWatchId !== null && typeof navigator !== 'undefined' && 'geolocation' in navigator) {
      navigator.geolocation.clearWatch(this.geoWatchId);
      this.geoWatchId = null;
    }
    if (this.wsSubscription) {
      this.wsSubscription.unsubscribe();
      this.wsSubscription = null;
    }
  }
}
