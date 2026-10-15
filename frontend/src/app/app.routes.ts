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
    pathMatch: 'full',
    canActivate: [roleGuard],
    data: { roles: [UserRole.STUDENT, 'STUDENT', 'ESTUDANTE'] },
    loadComponent: () =>
      import(
        './features/bus/components/bus-tracking/bus-tracking.component'
      ).then((m) => m.BusTrackingComponent)
  },
  {
    path: 'student/tracking',
    redirectTo: 'student',
    pathMatch: 'full'
  },
  {
    path: 'student/polls',
    canActivate: [roleGuard],
    data: { roles: [UserRole.STUDENT, 'STUDENT', 'ESTUDANTE'] },
    loadComponent: () =>
      import(
        './features/polls/components/student-polls/student-polls.component'
      ).then((m) => m.StudentPollsComponent)
  },
  {
    path: 'student/profile',
    canActivate: [roleGuard],
    data: { roles: [UserRole.STUDENT, 'STUDENT', 'ESTUDANTE'] },
    loadComponent: () =>
      import(
        './features/university-students/components/student-profile/student-profile.component'
      ).then((m) => m.StudentProfileComponent)
  },
  {
    path: 'driver',
    canActivate: [roleGuard],
    data: { roles: [UserRole.BUS_DRIVER, 'BUS_DRIVER', 'MOTORISTA'] },
    loadComponent: () =>
      import('./features/bus-drivers/components/driver-home/driver-home.component').then(
        (m) => m.DriverHomeComponent
      )
  },
  {
    path: 'driver/polls',
    canActivate: [roleGuard],
    data: { roles: [UserRole.BUS_DRIVER, 'BUS_DRIVER', 'MOTORISTA'] },
    loadComponent: () =>
      import('./features/bus-drivers/components/poll-selection/poll-selection.component').then(
        (m) => m.PollSelectionComponent
      )
  },
  {
    path: 'driver/prepare-travel',
    redirectTo: 'driver/polls',
    pathMatch: 'full'
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
