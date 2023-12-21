import { CanMatch, Route, Router, UrlSegment, UrlTree } from '@angular/router';
import { Store, select } from '@ngrx/store';
import { filter, first, switchMap } from 'rxjs/operators';
import { isAuthenticated, pendingAuthentication } from '../reducers';
import { Injectable } from '@angular/core';
import { SessionStorageService } from '@app/core';
import { State } from '../../core.state';

@Injectable()
export class IsNotAuthenticatedGuard implements CanMatch {
  constructor(private router: Router, private store: Store<State>, private sessionStorageService: SessionStorageService) {}

  canMatch(route: Route, segments: UrlSegment[]): Promise<boolean> {
    return new Promise(resolve => {
      this.store
        .pipe(
          select(pendingAuthentication),
          filter((pending: boolean) => !pending),
          switchMap(() => this.store.pipe(select(isAuthenticated), first()))
        )
        .subscribe(({ authenticated }) => {
          if (!authenticated) {
            const url = location.href.replace(location.origin, '').replace('/#', '');

            if (!['login', 'reset-password', 'information'].some((key: string) => url.includes(key))) {
              this.sessionStorageService.setItem('PREV_URL', url);
            }
          }

          return resolve(!authenticated);
        });
    });
  }
}
