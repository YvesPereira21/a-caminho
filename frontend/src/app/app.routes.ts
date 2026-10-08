import { Routes } from '@angular/router';
import { roleGuard } from './core/guards/role.guard';
import { UserRole } from './core/models';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'login'
  },
  {
    path: 'login',
    loadComponent: () =>
      import('./features/login/login.component').then((m) => m.LoginComponent)
  },
  {
    path: 'register',
    loadComponent: () =>
      import('./features/university-students/components/student-create/student-create.component').then(
        (m) => m.StudentCreateComponent
      )
  },
  {
    path: 'student',
    canActivate: [roleGuard],
    data: { roles: [UserRole.STUDENT, 'STUDENT', 'ESTUDANTE'] },
    loadComponent: () =>
      import('./features/placeholder/area-placeholder.component').then(
        (m) => m.AreaPlaceholderComponent
      )
  },
  {
    path: 'driver',
    canActivate: [roleGuard],
    data: { roles: [UserRole.BUS_DRIVER, 'BUS_DRIVER', 'MOTORISTA'] },
    loadComponent: () =>
      import('./features/placeholder/area-placeholder.component').then(
        (m) => m.AreaPlaceholderComponent
      )
  },
  {
    path: 'admin',
    canActivate: [roleGuard],
    data: {
      roles: [
        UserRole.ADMIN,
        'ADMIN',
        'ADMINISTRADOR',
        UserRole.MUNICIPALITY,
        'PREFEITURA',
        'MUNICIPALITY'
      ]
    },
    loadComponent: () =>
      import('./features/placeholder/area-placeholder.component').then(
        (m) => m.AreaPlaceholderComponent
      )
  },
  {
    path: 'demo',
    loadComponent: () =>
      import('./features/demo/demo.component').then((m) => m.DemoComponent)
  },
  {
    path: '**',
    redirectTo: 'login'
  }
];
