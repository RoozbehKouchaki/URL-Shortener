import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';

import { AuthService } from '../services/auth.service';

/** Signing in is how a token is obtained, so it must not carry one. */
const UNAUTHENTICATED_PATHS = ['/api/auth/'];

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);

  const isApiRequest = req.url.startsWith('/api');
  const isAuthEndpoint = UNAUTHENTICATED_PATHS.some((path) => req.url.startsWith(path));

  if (isApiRequest && !isAuthEndpoint) {
    const header = auth.getAuthHeader();
    if (header) {
      req = req.clone({ setHeaders: { Authorization: header } });
    }
  }

  return next(req);
};
