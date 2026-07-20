import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';

import { AuthService } from '../services/auth.service';

/**
 * Attaches the HTTP Basic {@code Authorization} header to every outgoing
 * {@code /api} request when credentials are available.
 *
 * <p>Centralizing this here means individual services and components never build
 * the auth header themselves. Non-{@code /api} requests (for example loading
 * assets) are passed through untouched.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);

  if (req.url.startsWith('/api')) {
    const header = auth.getAuthHeader();
    if (header) {
      req = req.clone({ setHeaders: { Authorization: header } });
    }
  }

  return next(req);
};
