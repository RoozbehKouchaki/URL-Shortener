/**
 * Client-side view of a Link, mirroring the backend {@code LinkResponse} DTO.
 * Internal fields such as {@code id} and {@code ownerUsername} are never exposed
 * by the API, so they are absent here too.
 */
export interface Link {
  shortCode: string;
  shortUrl: string;
  longUrl: string;
  active: boolean;
  clickCount: number;
  /** ISO-8601 instant string (for example {@code 2024-01-15T10:30:00Z}). */
  createdAt: string;
}

/**
 * Click statistics for a single Link, mirroring the backend
 * {@code LinkStatsResponse} DTO.
 */
export interface LinkStats {
  shortCode: string;
  clickCount: number;
}
