/**
 * The consistent error body returned by the backend, mirroring its
 * {@code ErrorResponse}. {@code field} is present only for errors tied to a
 * specific input field (for example a validation failure) and is omitted
 * otherwise.
 */
export interface ApiError {
  timestamp: string;
  status: number;
  message: string;
  field?: string;
}
