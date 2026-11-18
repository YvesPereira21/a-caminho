import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter, Router } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { routes } from '../../../../app.routes';
import { AuthService } from '../../../../core/auth/services/auth.service';
import { UserRole } from '../../../../core/models';
import { MunicipalityDriversComponent } from './municipality-drivers.component';

describe('Gestão de motoristas', () => {
  let auth: AuthService;
  let http: HttpTestingController;
  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideRouter(routes), provideHttpClient(), provideHttpClientTesting()] });
    auth = TestBed.inject(AuthService);
    http = TestBed.inject(HttpTestingController);
  });
  afterEach(() => http.verify());
  function login(role: UserRole): void {
    auth.saveToken('token', { id: '1', name: 'Guarabira', email: 'p@example.com', role });
  }
  it('restringe o acesso à prefeitura', async () => {
    login(UserRole.STUDENT);
    const harness = await RouterTestingHarness.create();
    await harness.navigateByUrl('/admin/drivers');
    expect(TestBed.inject(Router).url).toBe('/login');
  });
  it('carrega, pesquisa e cadastra motoristas usando os dados da API', async () => {
    login(UserRole.MUNICIPALITY);
    const harness = await RouterTestingHarness.create();
    const component = await harness.navigateByUrl('/admin/drivers', MunicipalityDriversComponent);
    http.expectOne(req => req.url.endsWith('/bus-drivers') && req.method === 'GET').flush([{ busDriverId: 'driver-1', busDriverName: 'José Silva', email: 'jose@example.com' }]);
    component.query.set('jose');
    expect(component.filtered().length).toBe(1);
    component.openCreate();
    component.form.setValue({ name: ' Maria ', email: 'maria@example.com', password: '123456' });
    component.save();
    const request = http.expectOne(req => req.url.endsWith('/bus-drivers') && req.method === 'POST');
    expect(request.request.body).toEqual({ busDriverName: 'Maria', user: { email: 'maria@example.com', password: '123456' } });
    request.flush({ message: 'Usuário já existente.' }, { status: 409, statusText: 'Conflict' });
    expect(component.error()).toBe('Usuário já existente.');
    component.save();
    http.expectOne(req => req.url.endsWith('/bus-drivers') && req.method === 'POST').flush({ busDriverId: 'driver-2', busDriverName: 'Maria', email: 'maria@example.com' });
    expect(component.drivers().length).toBe(2);
    expect(component.form.controls.password.value).toBe('');
  });
});
