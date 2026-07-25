import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map, tap } from 'rxjs';

import { LoginResponse } from '../models/auth';

interface Session {
  accessToken: string;
  tokenType: string;
  username: string;
  /** Epoch milliseconds. */
  expiresAt: number;
}

/** Kept in memory rather than web storage, so a page reload signs the user out. */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);

  private session: Session | null = null;

  login(username: string, password: string): Observable<void> {
    return this.http
      .post<LoginResponse>('/api/auth/login', { username, password })
      .pipe(
        tap((response) => {
          this.session = {
            accessToken: response.accessToken,
            tokenType: response.tokenType,
            username: response.username,
            expiresAt: Date.now() + response.expiresIn * 1000
          };
        }),
        map(() => undefined)
      );
  }

  clear(): void {
    this.session = null;
  }

  isAuthenticated(): boolean {
    return this.session !== null && Date.now() < this.session.expiresAt;
  }

  getUsername(): string | null {
    return this.session?.username ?? null;
  }

  getAuthHeader(): string | null {
    if (!this.session) {
      return null;
    }
    return `${this.session.tokenType} ${this.session.accessToken}`;
  }
}
