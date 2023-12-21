import { Injectable } from '@angular/core';
import { AuthService, AuthState, State } from '@app/core';
import * as AuthActions from '@app/core/auth/actions';
import { Store } from '@ngrx/store';

/**
 * Service to initialization application
 */
@Injectable()
export class AppInitializationService {
  constructor(private store: Store<State>, private authService: AuthService) {}

  /**
   * Auth: login user from token
   */
  auth(): void {
    const authState: AuthState = this.authService.getToken();

    if (authState && !!authState.jwt) {
      this.store.dispatch(AuthActions.login({ jwt: authState.jwt, initialization: true }));
    }
  }
}
