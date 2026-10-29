import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { of } from 'rxjs';
import { AuthService } from '../../../../core/auth/services/auth.service';
import { PollDirection, Travel, TravelStatus } from '../../../../core/models';
import { BusService } from '../../../bus/services/bus.service';
import { PollService } from '../../../polls/services/poll.service';
import { TravelService } from '../../../traveling/services/travel.service';
import { DriverHomeComponent } from './driver-home.component';

describe('DriverHomeComponent dashboard', () => {
  const travel = {
    travelId: 'travel-1', status: TravelStatus.IN_PROGRESS, direction: PollDirection.OUTBOUND,
    bus: { busId: 'bus-1', busName: 'Ônibus 01', seatsQuantity: 40 }, pollId: 'poll-1',
    confirmedVotesCount: 2, targetUniversities: [], busDriver: { busDriverName: 'Carlos' } as Travel['busDriver']
  } as Travel;
  let active: Travel | null;
  let recent: Travel[];

  beforeEach(() => {
    active = null;
    recent = [];
    TestBed.configureTestingModule({
      imports: [DriverHomeComponent],
      providers: [provideRouter([]),
        { provide: AuthService, useValue: { currentUser: signal({ name: 'Carlos', email: 'carlos@example.com' }) } },
        { provide: TravelService, useValue: { getActiveTravelByDriver: () => of(active), getAllTravelsByDriver: () => of(recent) } },
        { provide: BusService, useValue: { getAllBusesForDriver: () => of([travel.bus]) } },
        { provide: PollService, useValue: { getPoll: () => of({ routeName: 'Rota universitária', options: [{ optionId: '1', stopName: 'Centro', voteCount: 2 }] }) } }
      ]
    });
  });

  it('shows the active trip and its itinerary and opens trip management', () => {
    active = travel;
    const fixture = TestBed.createComponent(DriverHomeComponent);
    const navigate = spyOn(TestBed.inject(Router), 'navigate').and.resolveTo(true);
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Rota universitária');
    expect(fixture.nativeElement.textContent).toContain('Centro');
    expect(fixture.nativeElement.textContent).toContain('2 estudantes');
    (fixture.nativeElement.querySelector('.primary') as HTMLButtonElement).click();
    expect(navigate).toHaveBeenCalledWith(['/driver/travel-in-progress'], { state: { travel } });
  });

  it('allows a driver without an active trip to prepare a journey', () => {
    const fixture = TestBed.createComponent(DriverHomeComponent);
    const navigate = spyOn(TestBed.inject(Router), 'navigate').and.resolveTo(true);
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Prepare sua próxima viagem');
    (fixture.nativeElement.querySelector('.primary') as HTMLButtonElement).click();
    expect(navigate).toHaveBeenCalledWith(['/driver/polls']);
  });

  it('offers a return after an outbound trip completed today', () => {
    recent = [{ ...travel, status: TravelStatus.COMPLETED, travelDate: new Date().toISOString().split('T')[0] }];
    const fixture = TestBed.createComponent(DriverHomeComponent);
    const navigate = spyOn(TestBed.inject(Router), 'navigate').and.resolveTo(true);
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Iniciar viagem de volta');
    (fixture.nativeElement.querySelector('.primary') as HTMLButtonElement).click();
    expect(navigate).toHaveBeenCalledWith(['/driver/prepare-travel'], jasmine.objectContaining({ queryParams: { pollId: 'poll-1', direction: 'INBOUND' } }));
  });
});
