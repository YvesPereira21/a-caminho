import { ChangeDetectionStrategy, Component, computed, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AuthService } from '../../../../core/auth/services/auth.service';
import { TravelService } from '../../../traveling/services/travel.service';
import { BusService } from '../../../bus/services/bus.service';
import { Bus, isCompletedTravel, isOutboundTravel, Poll, PollDirection, Travel } from '../../../../core/models';
import { PollService } from '../../../polls/services/poll.service';
import { DriverHeaderComponent } from '../../../../shared/components/driver-header/driver-header.component';

@Component({
  selector: 'app-driver-home',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    DriverHeaderComponent
  ],
  templateUrl: './driver-home.component.html',
  styleUrl: './driver-home.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DriverHomeComponent implements OnInit {
  public readonly authService = inject(AuthService);
  private readonly travelService = inject(TravelService);
  private readonly busService = inject(BusService);
  private readonly pollService = inject(PollService);
  private readonly router = inject(Router);

  public readonly isLoading = signal<boolean>(true);
  public readonly hasError = signal<boolean>(false);
  public readonly activeTravel = signal<Travel | null>(null);
  public readonly activePoll = signal<Poll | null>(null);
  public readonly recentTravels = signal<Travel[]>([]);
  public readonly buses = signal<Bus[]>([]);
  public readonly notificationMessage = signal<string | null>(null);

  public readonly PollDirection = PollDirection;

  public readonly driverName = computed(() => {
    const user = this.authService.currentUser();
    if (!user) return 'Motorista';
    return user.name || user.email.split('@')[0];
  });

  public readonly driverEmail = computed(() => {
    return this.authService.currentUser()?.email || '';
  });

  public readonly hasActiveTravel = computed(() => {
    return this.activeTravel() !== null;
  });

  public readonly todayCompletedOutbound = computed<Travel | null>(() => {
    if (this.hasActiveTravel()) return null;
    const today = new Date().toISOString().split('T')[0];
    const travels = this.recentTravels();
    return travels.find((t) => {
      const isToday = !t.travelDate || t.travelDate === today || t.travelDate.startsWith(today);
      const isCompleted = isCompletedTravel(t.status);
      const isOut = isOutboundTravel(t.direction);
      const hasInboundForSamePoll = travels.some((other) =>
        other.pollId === t.pollId &&
        !isOutboundTravel(other.direction) &&
        isCompletedTravel(other.status)
      );
      return isToday && isCompleted && isOut && !hasInboundForSamePoll;
    }) || null;
  });

  public isOutbound(direction?: string | PollDirection | null): boolean {
    return isOutboundTravel(direction);
  }

  ngOnInit(): void {
    if (history.state?.notificationMessage) {
      this.notificationMessage.set(history.state.notificationMessage);
      setTimeout(() => this.notificationMessage.set(null), 6000);
    }
    this.loadDashboardData();
  }

  public onStartReturnTravel(): void {
    const outbound = this.todayCompletedOutbound();
    if (!outbound || !outbound.pollId) return;

    this.router.navigate(['/driver/prepare-travel'], {
      queryParams: {
        pollId: outbound.pollId,
        direction: 'INBOUND'
      },
      state: {
        direction: PollDirection.INBOUND,
        preselectedBusId: outbound.bus?.busId
      }
    });
  }

  public loadDashboardData(): void {
    this.isLoading.set(true);
    this.hasError.set(false);

    forkJoin({
      active: this.travelService.getActiveTravelByDriver().pipe(
        catchError(() => of(null)) // 404 significa que não há viagem ativa
      ),
      all: this.travelService.getAllTravelsByDriver().pipe(
        catchError(() => of([]))
      ),
      buses: this.busService.getAllBusesForDriver().pipe(
        catchError(() => of([]))
      )
    }).subscribe({
      next: ({ active, all, buses }) => {
        this.activeTravel.set(active);
        this.activePoll.set(null);
        if (active?.pollId) {
          this.pollService.getPoll(active.pollId).subscribe({
            next: (poll) => this.activePoll.set(poll),
            error: () => this.activePoll.set(null)
          });
        }
        // Filtra para que a viagem ativa não apareça duplicada no histórico de viagens anteriores
        const pastTravels = active
          ? all.filter((t) => t.travelId !== active.travelId)
          : all;
        this.recentTravels.set(pastTravels);
        this.buses.set(buses);
        this.isLoading.set(false);
      },
      error: () => {
        this.hasError.set(true);
        this.isLoading.set(false);
      }
    });
  }

  public onPrepareTravel(): void {
    this.router.navigate(['/driver/polls']);
  }

  public onManageTravel(travelId?: string): void {
    this.router.navigate(['/driver/travel-in-progress'], {
      state: { travel: this.activeTravel() }
    });
  }

  public onLogout(): void {
    this.authService.logout();
  }
}

