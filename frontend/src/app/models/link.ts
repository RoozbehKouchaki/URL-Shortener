/** Mirrors the backend {@code LinkResponse}. */
export interface Link {
  shortCode: string;
  shortUrl: string;
  longUrl: string;
  active: boolean;
  clickCount: number;
  /** ISO-8601 instant. */
  createdAt: string;
}
