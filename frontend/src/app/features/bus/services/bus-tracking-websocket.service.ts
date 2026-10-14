import { inject, Injectable, OnDestroy, signal } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { Client, StompSubscription } from '@stomp/stompjs';
import { environment } from '../../../environments/environment';
import { AuthService } from '../../../core/auth/services/auth.service';
import { BusLocation } from '../../../core/models';

@Injectable({
  providedIn: 'root'
})
export class BusTrackingWebSocketService implements OnDestroy {
  private readonly authService = inject(AuthService);

  private stompClient: Client | null = null;
  public readonly isConnected = signal<boolean>(false);
  public readonly connectionState = signal<'disconnected' | 'connecting' | 'connected' | 'error'>('disconnected');

  private readonly locationSubject = new Subject<{ topic: string; location: BusLocation }>();
  private readonly activeSubscriptions = new Map<string, StompSubscription>();
  private readonly desiredTopics = new Set<string>();

  constructor() {
    this.initClient();
  }

  private initClient(): void {
    try {
      const httpBase = environment.apiUrl.replace(/\/api\/?$/, '');
      const brokerURL = httpBase.replace(/^http/, 'ws') + '/stomp/websocket';

      this.stompClient = new Client({
        brokerURL,
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
        beforeConnect: () => {
          const token = this.authService.getToken();
          if (token && this.stompClient) {
            this.stompClient.connectHeaders = {
              Authorization: `Bearer ${token}`
            };
          }
        },
        onConnect: () => {
          this.isConnected.set(true);
          this.connectionState.set('connected');
          this.resubscribeTopics();
        },
        onDisconnect: () => {
          this.isConnected.set(false);
          this.connectionState.set('disconnected');
        },
        onStompError: (frame) => {
          console.error('Erro no STOMP:', frame?.headers ? frame.headers['message'] : '', frame?.body);
          this.isConnected.set(false);
          this.connectionState.set('error');
        },
        onWebSocketClose: () => {
          this.isConnected.set(false);
          this.connectionState.set('disconnected');
        }
      });
    } catch (e) {
      console.error('Erro ao inicializar cliente WebSocket STOMP:', e);
    }
  }

  public connect(): void {
    if (!this.stompClient) {
      this.initClient();
    }
    if (!this.stompClient?.active) {
      this.connectionState.set('connecting');
      this.stompClient?.activate();
    }
  }

  public disconnect(): void {
    if (this.stompClient?.active) {
      this.activeSubscriptions.forEach((sub) => sub.unsubscribe());
      this.activeSubscriptions.clear();
      this.stompClient.deactivate();
      this.isConnected.set(false);
      this.connectionState.set('disconnected');
    }
  }

  public listenBusLocation(busId: string): Observable<BusLocation> {
    const topic = `/topic/buses/${busId}/location`;
    this.ensureSubscribed(topic);

    return new Observable<BusLocation>((observer) => {
      const sub = this.locationSubject.subscribe(({ topic: receivedTopic, location }) => {
        if (receivedTopic === topic) {
          observer.next(location);
        }
      });

      return () => {
        sub.unsubscribe();
      };
    });
  }

  public listenAllLocations(): Observable<BusLocation> {
    const topic = '/topic/live-location';
    this.ensureSubscribed(topic);

    return new Observable<BusLocation>((observer) => {
      const sub = this.locationSubject.subscribe(({ topic: receivedTopic, location }) => {
        if (receivedTopic === topic) {
          observer.next(location);
        }
      });

      return () => {
        sub.unsubscribe();
      };
    });
  }

  private ensureSubscribed(topic: string): void {
    this.desiredTopics.add(topic);

    if (!this.stompClient?.active) {
      this.connect();
    }

    if (this.stompClient?.connected && !this.activeSubscriptions.has(topic)) {
      const stompSub = this.stompClient.subscribe(topic, (message) => {
        try {
          const location: BusLocation = JSON.parse(message.body);
          this.locationSubject.next({ topic, location });
        } catch (e) {
          console.error('Falha ao parsear payload de localização:', e);
        }
      });
      this.activeSubscriptions.set(topic, stompSub);
    }
  }

  private resubscribeTopics(): void {
    // Ao (re)conectar, assina todos os tópicos desejados (inclusive os pedidos antes da conexão)
    this.activeSubscriptions.clear();
    for (const topic of this.desiredTopics) {
      this.ensureSubscribed(topic);
    }
  }

  ngOnDestroy(): void {
    this.disconnect();
  }
}

