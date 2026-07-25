import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { AuthService } from '../services/auth.service';

/** Decides what to render only; the backend is what protects the data. */
export const authGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  return auth.isAuthenticated() ? true : router.createUrlTree(['/login']);
};
