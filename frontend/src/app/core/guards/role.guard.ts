import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../auth/services/auth.service';
import { UserRole } from '../models';

export const roleGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.isAuthenticated()) {
    return router.parseUrl('/login');
  }

  const expectedRoles = route.data['roles'] as (UserRole | string)[] | undefined;

  if (!expectedRoles || expectedRoles.length === 0) {
    return true;
  }

  if (authService.hasRole(...expectedRoles)) {
    return true;
  }

  return router.parseUrl('/');
};
