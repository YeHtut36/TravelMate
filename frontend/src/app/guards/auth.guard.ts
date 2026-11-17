import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { map } from 'rxjs';

/**
 * Protects private routes by requiring an authenticated user session.
 */
export const authGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isInitialized()) {
    if (authService.isAuthenticated()) {
      return true;
    }
    return router.createUrlTree(['/login']);
  }

  return authService.checkAuth().pipe(
    map((user) => {
      if (user) {
        return true;
      }
      return router.createUrlTree(['/login']);
    })
  );
};

/**
 * Prevents already-authenticated users from visiting /login or /register.
 */
export const guestGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isInitialized()) {
    if (!authService.isAuthenticated()) {
      return true;
    }
    return router.createUrlTree(['/dashboard']);
  }

  return authService.checkAuth().pipe(
    map((user) => {
      if (!user) {
        return true;
      }
      return router.createUrlTree(['/dashboard']);
    })
  );
};
