import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

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
