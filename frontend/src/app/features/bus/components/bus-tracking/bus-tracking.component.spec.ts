import { signal } from '@angular/core';
import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { Subject, of } from 'rxjs';
import * as L from 'leaflet';
import { AuthService } from '../../../../core/auth/services/auth.service';
import { BusLocation, PollDirection, Shift, Travel, TravelStatus } from '../../../../core/models';
import { PollService } from '../../../polls/services/poll.service';
import { TravelService } from '../../../traveling/services/travel.service';
import { BusService } from '../../services/bus.service';
import { BusTrackingWebSocketService } from '../../services/bus-tracking-websocket.service';
import { BusTrackingComponent } from './bus-tracking.component';

describe('BusTrackingComponent map lifecycle', () => {
  let fixture: ComponentFixture<BusTrackingComponent>;
  let component: BusTrackingComponent;
  let travels: Subject<Travel[]>;
  let locations: Subject<BusLocation>;
  let resizeCallback: ResizeObserverCallback;
  let resizeObserver: jasmine.SpyObj<ResizeObserver>;

  const location: BusLocation = { busId: 'bus-1', latitude: -6.854, longitude: -35.49 };
  const travel: Travel = {
    travelId: 'travel-1',
    status: TravelStatus.IN_PROGRESS,
    direction: PollDirection.OUTBOUND,
    bus: { busId: 'bus-1', busName: 'Ônibus Universitário 01 - Mercedes Benz', seatsQuantity: 40 },
    busDriver: { busDriverName: 'Carlos <Silva> dos Santos' } as Travel['busDriver'],
    pollId: 'poll-1',
    confirmedVotesCount: 1,
    targetUniversities: []
  };

  beforeEach(() => {
    travels = new Subject<Travel[]>();
    locations = new Subject<BusLocation>();
    resizeObserver = jasmine.createSpyObj('ResizeObserver', ['observe', 'unobserve', 'disconnect']);
    spyOn(window, 'ResizeObserver').and.callFake(function (callback: ResizeObserverCallback) {
      resizeCallback = callback;
      return resizeObserver;
    });

    TestBed.configureTestingModule({
      imports: [BusTrackingComponent],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: { currentUser: signal(null) } },
        { provide: TravelService, useValue: { getActiveTravelsForStudent: () => travels } },
        { provide: BusService, useValue: { getLatestLocation: () => of(location) } },
        { provide: PollService, useValue: {
          getOpenPollsToStudent: () => of([]),
          getPoll: () => of({
            pollId: 'poll-1', routeName: 'Guarabira → João Pessoa', shift: Shift.NIGHT,
            options: [{ optionId: 'stop-1', stopName: 'Praça <Central>', voteCount: 1,
              latitude: -6.85, longitude: -35.49 }]
          })
        } },
        { provide: BusTrackingWebSocketService, useValue: {
          connect: () => {},
          connectionState: signal('connected'),
          listenBusLocation: () => locations,
          listenAllLocations: () => locations
        } }
      ]
    });

    fixture = TestBed.createComponent(BusTrackingComponent);
    component = fixture.componentInstance;
    // Exercita o Leaflet real sem depender do provedor externo de tiles.
    component.mapOptions = { ...component.mapOptions, layers: [], zoomAnimation: false };
    const onMapReady = component.onMapReady.bind(component);
    spyOn(component, 'onMapReady').and.callFake((map: L.Map) => {
      // O iframe oculto do Karma não fornece as dimensões de uma janela real.
      map.getContainer().style.width = '600px';
      map.getContainer().style.height = '400px';
      onMapReady(map);
    });
    fixture.detectChanges();
  });

  afterEach(() => fixture.destroy());

  function showActiveTravel(): L.Map {
    travels.next([travel]);
    fixture.detectChanges();
    return component['leafletMap']!;
  }

  it('waits for delayed travel data before creating a map with visible dimensions', fakeAsync(() => {
    tick(1000);
    expect(fixture.nativeElement.querySelector('.leaflet-container')).toBeNull();

    const map = showActiveTravel();
    expect(map.getSize().x).toBeGreaterThan(0);
    expect(map.getSize().y).toBeGreaterThan(0);
    expect(component['stopMarkers'].length).toBe(1);
    expect(component['busMarker']!.getLatLng()).toEqual(L.latLng(location.latitude, location.longitude));
    tick(300);
  }));

  it('creates the map when the first live location arrives after the empty state', () => {
    travels.next([]);
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('.leaflet-container')).toBeNull();

    component.selectPoll('poll-1');
    locations.next(location);
    fixture.detectChanges();

    expect(component['leafletMap']!.getSize().x).toBeGreaterThan(0);
    expect(component['busMarker']).not.toBeNull();
  });

  it('keeps bus and stop details as text and uses a readable popup width', () => {
    showActiveTravel();
    const popup = component['busMarker']!.getPopup()!;
    const content = popup.getContent() as HTMLElement;

    expect(content.textContent).toContain('Carlos <Silva> dos Santos');
    expect(content.querySelector('silva')).toBeNull();
    expect(popup.getElement()!.querySelector('.leaflet-popup-content')!.clientWidth).toBeGreaterThanOrEqual(200);

    const stopContent = component['stopMarkers'][0].getPopup()!.getContent() as HTMLElement;
    expect(stopContent.textContent).toContain('Praça <Central>');
    expect(stopContent.querySelector('central')).toBeNull();
  });

  it('recalculates the map and popup when the container changes size', () => {
    const map = showActiveTravel();
    const updatePopup = spyOn(component['busMarker']!.getPopup()!, 'update').and.callThrough();
    map.getContainer().style.width = '320px';
    resizeCallback([{ contentRect: { width: 320, height: 600 } } as ResizeObserverEntry], resizeObserver);

    expect(map.getSize().x).toBe(320);
    expect(updatePopup).toHaveBeenCalled();
  });

  it('disconnects the resize observer when leaving the page', () => {
    showActiveTravel();
    fixture.destroy();
    expect(resizeObserver.disconnect).toHaveBeenCalled();
  });

  it('recreates the markers when the map returns after loading', () => {
    const firstMap = showActiveTravel();
    component.isLoading.set(true);
    fixture.detectChanges();
    component.isLoading.set(false);
    fixture.detectChanges();

    expect(component['leafletMap']).not.toBe(firstMap);
    expect(component['stopMarkers'].length).toBe(1);
    expect(component['busMarker']!.getPopup()!.isOpen()).toBeTrue();
    expect(resizeObserver.disconnect).toHaveBeenCalled();
  });
});
