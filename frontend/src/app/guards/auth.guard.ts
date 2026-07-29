import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const authGuard: CanActivateFn = (route, state) => {
  const router = inject(Router);
  const hasToken = !!sessionStorage.getItem('token');

  if (hasToken) {
    return true;
  }

  router.navigate(['/login']);
  return false;
};

export const guestGuard: CanActivateFn = (route, state) => {
  const router = inject(Router);
  const hasToken = !!sessionStorage.getItem('token');

  if (hasToken) {
    router.navigate(['/dashboard']);
    return false;
  }

  return true;
};

export const roleGuard: CanActivateFn = (route, state) => {
  const router = inject(Router);
  const authService = inject(AuthService);
  const allowedRoles = route.data?.['roles'] as string[];

  const user = authService.currentUser();
  if (!user) {
    router.navigate(['/login']);
    return false;
  }

  if (!allowedRoles || allowedRoles.includes(user.role)) {
    return true;
  }

  router.navigate(['/dashboard']);
  return false;
};
