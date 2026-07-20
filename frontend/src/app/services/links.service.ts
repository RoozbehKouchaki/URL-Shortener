import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { Link, LinkStats } from '../models/link';

/**
 * Thin wrapper over the four management API calls, returning typed results to
 * components. The HTTP Basic {@code Authorization} header is added centrally by
 * the auth interceptor, so this service never deals with credentials itself.
 */
@Injectable({ providedIn: 'root' })
export class LinksService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/links';

  /** Create a short link for the given Long URL. */
  create(longUrl: string): Observable<Link> {
    return this.http.post<Link>(this.baseUrl, { longUrl });
  }

  /** List the signed-in user's Links, newest first. */
  listMine(): Observable<Link[]> {
    return this.http.get<Link[]>(this.baseUrl);
  }

  /** Deactivate one of the signed-in user's Links. */
  deactivate(shortCode: string): Observable<Link> {
    return this.http.post<Link>(
      `${this.baseUrl}/${encodeURIComponent(shortCode)}/deactivate`,
      {}
    );
  }

  /** Read the click statistics for one of the signed-in user's Links. */
  stats(shortCode: string): Observable<LinkStats> {
    return this.http.get<LinkStats>(
      `${this.baseUrl}/${encodeURIComponent(shortCode)}/stats`
    );
  }
}
