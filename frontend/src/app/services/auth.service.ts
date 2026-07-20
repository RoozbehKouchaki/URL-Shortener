import { Injectable } from '@angular/core';

/**
 * Holds the signed-in Demo User's credentials in memory so the HTTP interceptor
 * can attach an HTTP Basic {@code Authorization} header to every {@code /api}
 * request.
 *
 * <p>Basic auth has no separate login endpoint and the backend is stateless, so
 * the credentials must be re-sent on every request. Keeping the raw credentials
 * on the client is a deliberate demo simplification; a production frontend would
 * sign in once and carry a short-lived token instead.
 *
 * <p>Credentials live only in memory, so a full page reload signs the user out.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private credentials: { username: string; password: string } | null = null;

  /** Store the credentials to be sent on subsequent {@code /api} requests. */
  setCredentials(username: string, password: string): void {
    this.credentials = { username, password };
  }

  /** Forget the stored credentials (sign out / failed validation). */
  clear(): void {
    this.credentials = null;
  }

  /** True once valid credentials have been stored. */
  isAuthenticated(): boolean {
    return this.credentials !== null;
  }

  /** The signed-in username, or null when signed out. */
  getUsername(): string | null {
    return this.credentials?.username ?? null;
  }

  /**
   * The {@code Authorization: Basic <base64(username:password)>} header value,
   * or null when no credentials are stored.
   */
  getAuthHeader(): string | null {
    if (!this.credentials) {
      return null;
    }
    const token = btoa(`${this.credentials.username}:${this.credentials.password}`);
    return `Basic ${token}`;
  }
}
