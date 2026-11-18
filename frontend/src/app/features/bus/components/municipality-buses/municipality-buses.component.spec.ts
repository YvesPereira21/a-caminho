import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter, Router } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { routes } from '../../../../app.routes';
import { AuthService } from '../../../../core/auth/services/auth.service';
import { UserRole } from '../../../../core/models';
import { MunicipalityBusesComponent } from './municipality-buses.component';

describe('Gestão de ônibus', () => {
  let auth: AuthService;
  let http: HttpTestingController;
  const bus = { busId: '1', busName: 'Ônibus 01', seatsQuantity: 44 };
  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideRouter(routes), provideHttpClient(), provideHttpClientTesting()] });
    auth = TestBed.inject(AuthService);
    http = TestBed.inject(HttpTestingController);
  });
  afterEach(() => http.verify());
  function login(role: UserRole): void {
    auth.saveToken('token', { id: '1', name: 'Guarabira', email: 'prefeitura@example.com', role });
  }
  it('restringe a rota a usuários da prefeitura', async () => {
    login(UserRole.STUDENT);
    const harness = await RouterTestingHarness.create();
    await harness.navigateByUrl('/admin/buses');
    expect(TestBed.inject(Router).url).toBe('/login');
  });
  it('permite cadastrar e editar, e apresenta o erro de exclusão da API', async () => {
    login(UserRole.MUNICIPALITY);
    const harness = await RouterTestingHarness.create();
    const component = await harness.navigateByUrl('/admin/buses', MunicipalityBusesComponent);
    http.expectOne(req => req.url.endsWith('/buses') && req.method === 'GET').flush([bus]);
    harness.detectChanges();
    component.open('create');
    component.form.setValue({ busName: '  Ônibus 02  ', seatsQuantity: 30 });
    component.save();
    const create = http.expectOne(req => req.url.endsWith('/buses') && req.method === 'POST');
    expect(create.request.body).toEqual({ busName: 'Ônibus 02', seatsQuantity: 30 });
    create.flush({ busId: '2', busName: 'Ônibus 02', seatsQuantity: 30 });
    expect(component.buses().length).toBe(2);
    component.open('edit', bus);
    component.form.setValue({ busName: 'Ônibus atualizado', seatsQuantity: 45 });
    component.save();
    http.expectOne(req => req.url.endsWith('/buses/1') && req.method === 'PUT').flush({ ...bus, busName: 'Ônibus atualizado', seatsQuantity: 45 });
    expect(component.buses()[0].busName).toBe('Ônibus atualizado');
    component.open('delete', component.buses()[0]);
    component.remove();
    http.expectOne(req => req.url.endsWith('/buses/1') && req.method === 'DELETE').flush({ message: 'Há viagens vinculadas.' }, { status: 400, statusText: 'Bad Request' });
    expect(component.error()).toBe('Há viagens vinculadas.');
    expect(component.buses().length).toBe(2);
    component.close();
  });
});
