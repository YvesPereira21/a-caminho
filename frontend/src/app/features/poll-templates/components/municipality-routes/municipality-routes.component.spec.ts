import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter, Router } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { routes } from '../../../../app.routes';
import { AuthService } from '../../../../core/auth/services/auth.service';
import { UserRole } from '../../../../core/models';
import { MunicipalityRoutesComponent } from './municipality-routes.component';

describe('Gestão de rotas', () => {
  let auth: AuthService;
  let http: HttpTestingController;
  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideRouter(routes), provideHttpClient(), provideHttpClientTesting()] });
    auth = TestBed.inject(AuthService); http = TestBed.inject(HttpTestingController);
  });
  afterEach(() => http.verify());
  it('protege a rota para a prefeitura', async () => {
    auth.saveToken('token', { id: '1', name: 'Aluno', email: 'a@example.com', role: UserRole.STUDENT });
    const harness = await RouterTestingHarness.create();
    await harness.navigateByUrl('/admin/routes');
    expect(TestBed.inject(Router).url).toBe('/login');
  });
  it('envia o cadastro com paradas ordenadas e apresenta erros da API', async () => {
    auth.saveToken('token', { id: '1', name: 'Guarabira', email: 'p@example.com', role: UserRole.MUNICIPALITY });
    const harness = await RouterTestingHarness.create();
    const component = await harness.navigateByUrl('/admin/routes', MunicipalityRoutesComponent);
    http.expectOne(req => req.url.endsWith('/poll-templates')).flush([]);
    harness.detectChanges();
    component.open(null);
    http.expectOne(req => req.url.endsWith('/poll-options')).flush([{ optionId: 'a', stopName: 'A' }, { optionId: 'b', stopName: 'B' }]);
    http.expectOne(req => req.url.endsWith('/universities')).flush([{ id: 'u', name: 'Universidade' }]);
    component.form.setValue({ routeName: 'Rota', shift: 'MANHA', start: '06:00', end: '08:00' });
    component.toggleUniversity('u', true); component.addStop('a'); component.addStop('b'); component.moveStop(1, -1);
    component.save();
    const request = http.expectOne(req => req.url.endsWith('/poll-templates') && req.method === 'POST');
    expect(request.request.body.defaultOptionIds).toEqual(['b', 'a']);
    request.flush({ message: 'Rota duplicada' }, { status: 409, statusText: 'Conflict' });
    expect(component.error()).toBe('Rota duplicada');
    component.close();
  });
});
