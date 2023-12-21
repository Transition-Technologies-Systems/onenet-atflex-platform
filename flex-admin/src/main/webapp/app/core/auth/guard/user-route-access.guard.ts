import { ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot } from '@angular/router';
import { Store, select } from '@ngrx/store';

import { AuthService } from '../auth.service';
import { Injectable } from '@angular/core';
import { State } from '@app/core';
import { first } from 'rxjs/operators';
import { isAuthenticated } from '../reducers';

@Injectable()
export class UserRouteAccessService implements CanActivate {
  constructor(private router: Router, private store: Store<State>, private authService: AuthService) {}

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Promise<boolean> {
    return new Promise(resolve =>
      Promise.all([
        this.checkRoles(route.data.roles || []),
        this.checkAuthorities(route.data.authorities || []),
        this.checkAnyAuthorities(route.data.anyAuthorities || []),
      ]).then((results: boolean[]) => {
        const hasAuthorities = results.every((result: boolean) => result);

        if (!hasAuthorities) {
          this.router.navigate(['/']);
          resolve(hasAuthorities);
          return;
        }

        this.store.pipe(select(isAuthenticated), first()).subscribe(({ passwordChangeOnFirstLogin }) => {
          if (passwordChangeOnFirstLogin && !state.url.includes('account/change-password')) {
            this.router.navigate(['/account/change-password']);
          }

          return resolve(hasAuthorities);
        });
      })
    );
  }

  checkAuthorities(authorities: string[]): Promise<boolean> {
    if (!authorities.length) {
      return Promise.resolve(true);
    }

    return new Promise(resolve => {
      Promise.all(authorities.map((authority: string) => this.authService.hasAuthority(authority))).then((hasAuthorities: boolean[]) =>
        resolve(hasAuthorities.every(Boolean))
      );
    });
  }

  checkAnyAuthorities(authorities: string[]): Promise<boolean> {
    if (!authorities.length) {
      return Promise.resolve(true);
    }

    return new Promise(resolve => {
      Promise.all(authorities.map((authority: string) => this.authService.hasAuthority(authority))).then((hasAuthorities: boolean[]) =>
        resolve(hasAuthorities.some(Boolean))
      );
    });
  }

  checkRoles(roles: string[]): Promise<boolean> {
    if (!roles.length) {
      return Promise.resolve(true);
    }

    return new Promise(resolve => {
      Promise.all(roles.map((role: string) => this.authService.hasRole(role))).then((hasRole: boolean[]) => resolve(hasRole.some(Boolean)));
    });
  }
}
