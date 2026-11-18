import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter, Router } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { of } from 'rxjs';
import { routes } from '../../../../app.routes';
import { AuthService } from '../../../../core/auth/services/auth.service';
import { UserRole } from '../../../../core/models';
import { BusService } from '../../../bus/services/bus.service';
import { PollTemplateService } from '../../../poll-templates/services/poll-template.service';
import { TravelService } from '../../../traveling/services/travel.service';
import { MunicipalityDashboardComponent } from './municipality-dashboard.component';
import { AreaPlaceholderComponent } from '../../../placeholder/area-placeholder.component';

describe('Navegação do dashboard da prefeitura', () => {
  let auth: AuthService;
  let buses: jasmine.Spy;

  beforeEach(() => {
    buses = jasmine.createSpy('getAllBusesByMunicipality').and.returnValue(of([]));
    TestBed.configureTestingModule({ providers: [
      provideRouter(routes), provideHttpClient(), provideHttpClientTesting(),
      { provide: BusService, useValue: { getAllBusesByMunicipality: buses, getMunicipalityLocations: () => of({}) } },
      { provide: PollTemplateService, useValue: { getAllPollTemplates: () => of([]) } },
      { provide: TravelService, useValue: { getAllTravelsByMunicipality: () => of([]) } }
    ] });
    auth = TestBed.inject(AuthService);
  });

  function login(role: UserRole): void {
    auth.saveToken('test-token', { id: '1', name: 'Guarabira', email: 'teste@example.com', role });
  }

  it('redireciona usuários sem sessão para login', async () => {
    const harness = await RouterTestingHarness.create();
    await harness.navigateByUrl('/admin');
    expect(TestBed.inject(Router).url).toBe('/login');
  });

  it('abre o painel da prefeitura e protege a rota contra estudantes', async () => {
    login(UserRole.MUNICIPALITY);
    const harness = await RouterTestingHarness.create();
    await harness.navigateByUrl('/admin', MunicipalityDashboardComponent);
    expect(harness.routeNativeElement?.textContent).toContain('Prefeitura de Guarabira');
    login(UserRole.STUDENT);
    await harness.navigateByUrl('/login');
    await harness.navigateByUrl('/admin');
    expect(TestBed.inject(Router).url).toBe('/login');
  });

  it('preserva a rota do administrador', async () => {
    login(UserRole.ADMIN);
    const harness = await RouterTestingHarness.create();
    await harness.navigateByUrl('/admin', AreaPlaceholderComponent);
    expect(TestBed.inject(Router).url).toBe('/admin');
  });

  it('navega entre o início e a tela de ônibus', async () => {
    login(UserRole.MUNICIPALITY);
    const harness = await RouterTestingHarness.create();
    await harness.navigateByUrl('/admin', MunicipalityDashboardComponent);
    const dashboard = harness.routeNativeElement as HTMLElement;
    dashboard.querySelector<HTMLButtonElement>('[routerLink="/admin/buses"]')!.click();
    await harness.fixture.whenStable();
    harness.detectChanges();
    expect(TestBed.inject(Router).url).toBe('/admin/buses');
    const busesPage = harness.routeNativeElement as HTMLElement;
    busesPage.querySelector<HTMLButtonElement>('[routerLink="/admin"]')!.click();
    await harness.fixture.whenStable();
    expect(TestBed.inject(Router).url).toBe('/admin');
  });

  it('abre e fecha o menu e atualiza os dados; mantém módulos futuros desativados', async () => {
    login(UserRole.MUNICIPALITY);
    const harness = await RouterTestingHarness.create();
    await harness.navigateByUrl('/admin', MunicipalityDashboardComponent);
    const root = harness.routeNativeElement as HTMLElement;
    const menu = root.querySelector<HTMLButtonElement>('.menu-button')!;
    const sidebar = root.querySelector<HTMLElement>('.sidebar')!;
    menu.click(); harness.detectChanges();
    expect(menu.getAttribute('aria-expanded')).toBe('true');
    expect(sidebar.classList.contains('open')).toBeTrue();
    root.querySelector<HTMLButtonElement>('.close-menu')!.click(); harness.detectChanges();
    expect(menu.getAttribute('aria-expanded')).toBe('false');
    expect(sidebar.classList.contains('open')).toBeFalse();
    expect(root.querySelectorAll('nav button:disabled').length).toBe(7);
    root.querySelector<HTMLButtonElement>('.refresh')!.click();
    expect(buses).toHaveBeenCalledTimes(2);
  });

  it('encerra a sessão e retorna ao login mesmo se a API falhar', async () => {
    login(UserRole.MUNICIPALITY);
    const harness = await RouterTestingHarness.create();
    await harness.navigateByUrl('/admin', MunicipalityDashboardComponent);
    (harness.routeNativeElement as HTMLElement).querySelector<HTMLButtonElement>('.logout')!.click();
    TestBed.inject(HttpTestingController).expectOne(req => req.url.endsWith('/auth/logout')).flush({}, { status: 500, statusText: 'Erro' });
    await harness.fixture.whenStable();
    expect(auth.isAuthenticated()).toBeFalse();
    expect(TestBed.inject(Router).url).toBe('/login');
  });
});
