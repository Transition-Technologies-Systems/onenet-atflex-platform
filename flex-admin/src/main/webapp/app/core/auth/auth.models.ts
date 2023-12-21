import { UserDTO } from '@app/shared/models';

export const AUTH_KEY = 'AUTH_TOKEN';

/**
 * Auth state
 */
export interface AuthState {
  /**
   * Authenticated status
   */
  isAuthenticated: boolean;

  /**
   * User data
   */
  user: UserDTO;

  /**
   * JWT token
   */
  jwt: string;
}
