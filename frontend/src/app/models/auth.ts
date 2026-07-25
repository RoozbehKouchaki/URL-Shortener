export interface LoginResponse {
  accessToken: string;
  tokenType: string;
  /** Seconds. */
  expiresIn: number;
  username: string;
}
