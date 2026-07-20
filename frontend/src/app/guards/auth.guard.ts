import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { AuthService } from '../services/auth.service';

/**
 * Keeps unauthenticated users out of protected routes. When no credentials are
 * stored, navigation is redirected to the login view; otherwise it proceeds.
 *
 * Credentials live only in memory, so after a full page reload the user is
 * treated as signed out and sent back to login.
 */
export const authGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  return auth.isAuthenticated() ? true : router.createUrlTree(['/login']);
};
