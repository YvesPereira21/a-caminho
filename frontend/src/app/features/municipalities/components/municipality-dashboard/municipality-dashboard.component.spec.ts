import { TestBed } from '@angular/core/testing';
import { signal } from '@angular/core';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { MunicipalityDashboardComponent } from './municipality-dashboard.component';
import { AuthService } from '../../../../core/auth/services/auth.service';
import { BusService } from '../../../bus/services/bus.service';
import { PollTemplateService } from '../../../poll-templates/services/poll-template.service';
import { TravelService } from '../../../traveling/services/travel.service';

describe('MunicipalityDashboardComponent', () => {
  const bus = { busId: 'bus-1', busName: 'Ônibus 1', seatsQuantity: 40 };
  let busService: { getAllBusesByMunicipality: jasmine.Spy; getMunicipalityLocations: jasmine.Spy };
  let travelService: { getAllTravelsByMunicipality: jasmine.Spy };

  beforeEach(() => {
    busService = {
      getAllBusesByMunicipality: jasmine.createSpy().and.returnValue(of([bus])),
      getMunicipalityLocations: jasmine.createSpy().and.returnValue(of({}))
    };
    travelService = { getAllTravelsByMunicipality: jasmine.createSpy().and.returnValue(of([])) };
    TestBed.configureTestingModule({
      imports: [MunicipalityDashboardComponent],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: { currentUser: signal({ name: 'Guarabira', email: 'prefeitura@example.com' }) } },
        { provide: BusService, useValue: busService },
        { provide: PollTemplateService, useValue: { getAllPollTemplates: () => of([]) } },
        { provide: TravelService, useValue: travelService }
      ]
    });
  });

  it('identifica a prefeitura e conta ônibus distintos em viagens em andamento', () => {
    travelService.getAllTravelsByMunicipality.and.returnValue(of([
      { status: 'Em andamento', bus }, { status: 'IN_PROGRESS', bus },
      { status: 'Concluída', bus: { ...bus, busId: 'bus-2' } }
    ]));
    const fixture = TestBed.createComponent(MunicipalityDashboardComponent);
    fixture.detectChanges();
    expect(fixture.componentInstance.municipalityLabel()).toBe('Prefeitura de Guarabira');
    expect(fixture.componentInstance.activeBusCount()).toBe(1);
    expect(fixture.nativeElement.textContent).toContain('Ônibus 1');
  });

  it('não mostra posições de veículos fora da frota nem coordenadas inválidas', () => {
    const fixture = TestBed.createComponent(MunicipalityDashboardComponent);
    const component = fixture.componentInstance;
    component.buses.set([bus]);
    component.locations.set({
      'bus-1': { busId: 'bus-1', latitude: -6.85, longitude: -35.49 },
      'bus-2': { busId: 'bus-2', latitude: -7, longitude: -35 }
    });
    expect(component.visibleLocations().map(item => item.bus.busId)).toEqual(['bus-1']);
    component.locations.set({ 'bus-1': { busId: 'bus-1', latitude: 200, longitude: -35 } });
    expect(component.visibleLocations()).toEqual([]);
  });

  it('informa falha na consulta sem apresentar zero como resultado', () => {
    busService.getAllBusesByMunicipality.and.returnValue(throwError(() => new Error('indisponível')));
    const fixture = TestBed.createComponent(MunicipalityDashboardComponent);
    fixture.detectChanges();
    expect(fixture.componentInstance.buses()).toBeNull();
    expect(fixture.componentInstance.hasErrors()).toBeTrue();
    expect(fixture.nativeElement.textContent).toContain('Frota indisponível');
  });
});
