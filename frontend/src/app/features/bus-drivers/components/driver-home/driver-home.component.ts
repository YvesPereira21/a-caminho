import { ChangeDetectionStrategy, Component, computed, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AuthService } from '../../../../core/auth/services/auth.service';
import { TravelService } from '../../../traveling/services/travel.service';
import { BusService } from '../../../bus/services/bus.service';
import { Bus, PollDirection, Travel } from '../../../../core/models';
import { DriverHeaderComponent } from '../../../../shared/components/driver-header/driver-header.component';
import { ActiveTravelCardComponent } from '../../../traveling/components/active-travel-card/active-travel-card.component';
import { PrepareTravelCardComponent } from '../../../traveling/components/prepare-travel-card/prepare-travel-card.component';

@Component({
  selector: 'app-driver-home',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    DriverHeaderComponent,
    ActiveTravelCardComponent,
    PrepareTravelCardComponent
  ],
  templateUrl: './driver-home.component.html',
  styleUrl: './driver-home.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DriverHomeComponent implements OnInit {
  public readonly authService = inject(AuthService);
  private readonly travelService = inject(TravelService);
  private readonly busService = inject(BusService);
  private readonly router = inject(Router);

  public readonly isLoading = signal<boolean>(true);
  public readonly hasError = signal<boolean>(false);
  public readonly activeTravel = signal<Travel | null>(null);
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

  ngOnInit(): void {
    this.loadDashboardData();
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
        this.recentTravels.set(all);
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

  public onManageTravel(travelId: string): void {
    this.notificationMessage.set(`Acessando painel da viagem: ${travelId}`);
    setTimeout(() => this.notificationMessage.set(null), 4000);
  }

  public onLogout(): void {
    this.authService.logout();
  }
}

