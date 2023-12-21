import * as AuthActions from '@app/core/auth/actions';

import { Actions, createEffect, ofType } from '@ngrx/effects';
import { State, getAuthState } from '../../core.state';
import { Store, select } from '@ngrx/store';
import { catchError, map, switchMap, tap, withLatestFrom } from 'rxjs/operators';

import { AUTH_KEY } from '../auth.models';
import { AppService } from '@app/app.service';
import { AuthService } from '../auth.service';
import { Injectable } from '@angular/core';
import { LocalStorageService } from '../../storage/local-storage.service';
import { Router } from '@angular/router';
import { SessionStorageService } from '../../storage/session-storage.service';
import { UserDTO } from '@app/shared/models';
import { of } from 'rxjs';

@Injectable()
export class AuthEffects {
  /**
   * Login effect - login user by JWT
   */
  login$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AuthActions.login),
      switchMap(({ jwt, initialization }) => {
        this.localStorageService.setItem(AUTH_KEY, { jwt });

        const loginUser = () =>
          this.authService.getUser(jwt, initialization).pipe(
            map(([{ user }]) => {
              const { authorities, ...rest } = user;
              const userData = rest as UserDTO;

              this.localStorageService.setItem(AUTH_KEY, { jwt });
              AppService.userId = user.id;

              return AuthActions.loginSuccess({
                jwt,
                user: userData,
                initialization: !!initialization,
              });
            }),
            catchError(error => of(AuthActions.loginFailure()))
          );

        return loginUser();
      })
    )
  );

  /**
   * Success login - set state to localstorage
   */
  loginSuccess$ = createEffect(
    () =>
      this.actions$.pipe(
        ofType(AuthActions.loginSuccess),
        withLatestFrom(this.store.pipe(select(getAuthState))),
        tap(([{ initialization }, authState]) => {
          const { ...state } = authState;
          const prevUrl = this.sessionStorage.getItem('PREV_URL');
          const url = (location.pathname + location.hash).replace('/#', '');
          const changeRoute = url.indexOf('/login') !== -1 || url.indexOf('/authenticate') !== -1;

          this.localStorageService.setItem(AUTH_KEY, state);

          if (authState.isAuthenticated && authState.user?.passwordChangeOnFirstLogin) {
            this.router.navigate(['/account/change-password']);
          } else if (!initialization || (changeRoute && authState.isAuthenticated)) {
            const redirectUrlData = prevUrl ? prevUrl : changeRoute ? '/' : url;
            const [redirectUrl, params = ''] = redirectUrlData.split('?');

            const parameters = params.split('&').reduce((currentParams: { [key: string]: string }, paramValue: string) => {
              const [key, value] = paramValue.split('=');

              return {
                ...currentParams,
                [key]: value,
              };
            }, {});

            this.router.navigate([redirectUrl], { queryParams: parameters });
          }
        })
      ),
    { dispatch: false }
  );

  /**
   * Failed login oraz logout - remove AUTH_KEY from localstorage and redirect
   */
  loginRedirect$ = createEffect(
    () =>
      this.actions$.pipe(
        ofType(AuthActions.loginFailure, AuthActions.logout, AuthActions.invalidJWT),
        tap(() => {
          new Promise(resolve => {
            this.localStorageService.setItem(AUTH_KEY, {
              isAuthenticated: false,
            });
            this.sessionStorage.clear();

            resolve(true);
          }).then(() => this.router.navigate(['/login']));
        })
      ),
    { dispatch: false }
  );

  /**
   * Invalid JWT - logout
   */
  invalidJWT$ = createEffect(
    () =>
      this.actions$.pipe(
        ofType(AuthActions.invalidJWT),
        tap(() => {
          this.localStorageService.setItem(AUTH_KEY, {
            isAuthenticated: false,
            pendingAuthentication: false,
          });
        })
      ),
    { dispatch: false }
  );

  constructor(
    private router: Router,
    private actions$: Actions,
    private store: Store<State>,
    private authService: AuthService,
    private sessionStorage: SessionStorageService,
    private localStorageService: LocalStorageService
  ) {}
}
