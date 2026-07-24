/** Mirrors the backend {@code ErrorResponse}. */
export interface ApiError {
  timestamp: string;
  status: number;
  message: string;
  field?: string;
}
