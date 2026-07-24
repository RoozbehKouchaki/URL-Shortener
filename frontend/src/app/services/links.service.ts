import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { Link, LinkStats } from '../models/link';

/** The Authorization header is added by the auth interceptor, not here. */
@Injectable({ providedIn: 'root' })
export class LinksService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/links';

  create(longUrl: string): Observable<Link> {
    return this.http.post<Link>(this.baseUrl, { longUrl });
  }

  listMine(): Observable<Link[]> {
    return this.http.get<Link[]>(this.baseUrl);
  }

  deactivate(shortCode: string): Observable<Link> {
    return this.http.post<Link>(
      `${this.baseUrl}/${encodeURIComponent(shortCode)}/deactivate`,
      {}
    );
  }

  stats(shortCode: string): Observable<LinkStats> {
    return this.http.get<LinkStats>(
      `${this.baseUrl}/${encodeURIComponent(shortCode)}/stats`
    );
  }
}
