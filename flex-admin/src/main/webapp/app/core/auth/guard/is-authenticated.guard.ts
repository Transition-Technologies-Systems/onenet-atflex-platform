import { Injectable } from '@angular/core';
import { CanMatch, Route, Router, UrlSegment } from '@angular/router';
import { SessionStorageService } from '@app/core/storage/session-storage.service';
import { Store, select } from '@ngrx/store';
import { filter, switchMap, first } from 'rxjs';
import { isAuthenticated, pendingAuthentication, State } from '../reducers';

@Injectable()
export class IsAuthenticatedGuard implements CanMatch {
  constructor(private router: Router, private store: Store<State>, private sessionStorageService: SessionStorageService) {}

  /**
   * Checks whether the user is authenticated
   *
   * @returns Observable boolean with authenticated status
   */
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

          return resolve(authenticated);
        });
    });
  }
}
