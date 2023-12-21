import * as LanguageActions from '@app/core/language/actions';

import { AUTH_KEY, AuthState } from './auth.models';
import { Observable, of } from 'rxjs';
import { State, getLanguageState } from '../core.state';
import { Store, select } from '@ngrx/store';
import { catchError, first, map, tap, withLatestFrom } from 'rxjs/operators';

import { AppService } from '@app/app.service';
import { Helpers } from '@app/shared/commons/helpers';
import { HttpClient } from '@angular/common/http';
import { HttpService } from '../http/http.service';
import { Injectable } from '@angular/core';
import { Language } from '@app/shared/enums';
import { LocalStorageService } from '../storage/local-storage.service';
import { UserDTO } from '@app/shared/models';
import { isAuthenticated } from './reducers';

interface AccountResponse {
  user: UserDTO;
  jwt: string;
  initialization: boolean;
}

interface PermissionsData {
  authorities: string[];
  roles: string[];
}

@Injectable()
export class AuthService extends HttpService {
  private accountJwt: string | null = null;
  private pendingUserAuthorities: Promise<PermissionsData> | undefined;
  private accountObservable: Observable<AccountResponse> | null = null;
  private permissions: PermissionsData = { authorities: [], roles: [] };

  constructor(private localStorage: LocalStorageService, private store: Store<State>, httpClient: HttpClient) {
    super(httpClient);
  }

  /**
   * Get JWT token
   */
  getToken(): AuthState {
    return this.localStorage.getItem(AUTH_KEY);
  }

  /**
   * Get user data
   *
   * @param jwt The user JWT
   * @param initialization Is initialization
   */
  getUser(jwt: string, initialization: boolean = false): Observable<[AccountResponse, Language]> {
    return this.getAccount(jwt, initialization).pipe(
      withLatestFrom(this.store.pipe(select(getLanguageState))),
      tap(([{ user }, key]) => {
        this.permissions = this.mapAuthorities(user);

        AppService.fspId = user.fspId;

        this.store.dispatch(LanguageActions.languageChange({ key, userLangKey: user.langKey }));
      })
    );
  }

  /**
   * Get user authorities
   */
  getUserAuthorities(): Promise<PermissionsData> {
    const authState: AuthState = this.getToken();

    if (!this.pendingUserAuthorities) {
      this.pendingUserAuthorities = new Promise(resolve => {
        this.getAccount(authState.jwt)
          .pipe(
            map(({ user }: AccountResponse) => this.mapAuthorities(user)),
            catchError(() => of({ authorities: [], roles: [] }))
          )
          .subscribe(resolve);
      });
    }

    return this.pendingUserAuthorities;
  }

  /**
   * Check if the user has permission
   *
   * @param authority The authority to check
   */
  hasAuthority(authorityValue: string): Promise<boolean> {
    const notAuthority = !!authorityValue.startsWith('!');
    const authority = notAuthority ? authorityValue.substring(1, authorityValue.length) : authorityValue;

    return new Promise(resolve => {
      this.store.pipe(select(isAuthenticated), first()).subscribe(({ authenticated }) => {
        if (!authenticated) {
          resolve(false);
        } else if (authority && authority.length) {
          this.identity().then(({ authorities, roles }: PermissionsData) => {
            this.permissions = { authorities, roles };
            resolve(notAuthority ? !authorities.includes(authority) : authorities.includes(authority));
          });
        } else {
          resolve(true);
        }
      });
    });
  }

  hasAnyAuthority(authorities: string[]): Promise<boolean> {
    if (authorities.length === 0) {
      return Promise.resolve(true);
    }

    return Helpers.someAsync(authorities, value => this.hasAuthority(value));
  }

  hasEveryAuthority(authorities: string[]): Promise<boolean> {
    if (authorities.length === 0) {
      return Promise.resolve(true);
    }

    return Helpers.everyAsync(authorities, value => this.hasAuthority(value));
  }

  /**
   * Check if the user has permission
   *
   * @param roles The roles to check
   */
  hasRole(roleValue: string): Promise<boolean> {
    const notRole = !!roleValue.startsWith('!');
    const role = notRole ? roleValue.substring(1, roleValue.length) : roleValue;

    return new Promise(resolve => {
      this.store.pipe(select(isAuthenticated), first()).subscribe(({ authenticated }) => {
        if (!authenticated) {
          resolve(false);
        } else if (role && role.length) {
          this.identity().then(({ authorities, roles }: PermissionsData) => {
            this.permissions = { authorities, roles };

            resolve(notRole ? !roles.includes(role) : roles.includes(role));
          });
        } else {
          resolve(true);
        }
      });
    });
  }

  hasAnyRoles(roles: string[]): Promise<boolean> {
    return Helpers.someAsync(roles, value => this.hasRole(value));
  }

  hasEveryRoles(roles: string[]): Promise<boolean> {
    return Helpers.everyAsync(roles, value => this.hasRole(value));
  }

  private getAccount(jwt: string, initialization: boolean = false): Observable<AccountResponse> {
    if (this.accountJwt === jwt && this.accountObservable) {
      return this.accountObservable;
    }

    this.accountObservable = this.get<UserDTO>('api/account', {
      headers: {
        Authorization: `Bearer ${jwt}`,
      },
    }).pipe(
      map((user: UserDTO) => {
        return { user, jwt, initialization };
      })
    );

    return this.accountObservable;
  }

  /**
   * Identity user and return authorities
   */
  private identity(): Promise<PermissionsData> {
    if (this.permissions) {
      return Promise.resolve(this.permissions);
    }

    const authState: AuthState = this.getToken();

    if (authState && !!authState.jwt) {
      return this.getUserAuthorities();
    }

    return Promise.resolve({ authorities: [], roles: [] });
  }

  private mapAuthorities(user: UserDTO): PermissionsData {
    return {
      authorities: user ? (user.authorities ? user.authorities.concat(['ROLE_USER']) : ['ROLE_USER']) : [],
      roles: user?.roles || [],
    };
  }
}
