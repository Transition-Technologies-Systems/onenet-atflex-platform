import { filter, first, switchMap } from 'rxjs/operators';

import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot } from '@angular/router';
import { State } from '@app/core';
import { getUserData, pendingAuthentication } from '@app/core/auth/reducers';
import { UserDTO } from '@app/shared/models';
import { select, Store } from '@ngrx/store';

@Injectable()
export class IsRegistrationUserGuard implements CanActivate {
  constructor(private router: Router, private store: Store<State>) {}

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Promise<boolean> {
    return new Promise(resolve => {
      this.store
        .pipe(
          select(pendingAuthentication),
          filter((pending: boolean) => !pending),
          switchMap(() => this.store.pipe(select(getUserData), first()))
        )
        .subscribe((user: UserDTO | undefined) => {
          const isRegistartionUser = user?.roles?.includes('ROLE_FSP_USER_REGISTRATION');

          if (isRegistartionUser && !state.url.includes('registration') && !state.url.includes('account/change-password')) {
            this.router.navigate(['/registration-thread']);
          }

          return resolve(true);
        });
    });
  }
}
