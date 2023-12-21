import { ActivatedRouteSnapshot, ActivationEnd, Router } from '@angular/router';
import { Observable, merge } from 'rxjs';
import { State, getLanguageState } from '../core.state';
import { Store, select } from '@ngrx/store';
import { distinctUntilChanged, filter, tap } from 'rxjs/operators';

import { Injectable } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { TranslateService } from '@ngx-translate/core';
import { environment as env } from '@env/environment';

@Injectable()
export class TitleService {
  /**
   * Observer who changes the title when language or route changes
   */
  changeApplicationTitle$: Observable<any> = merge(
    this.store.pipe(select(getLanguageState), distinctUntilChanged()),
    this.router.events.pipe(filter(event => event instanceof ActivationEnd))
  ).pipe(tap(() => this.setTitle(this.router.routerState.snapshot.root)));

  constructor(private title: Title, private router: Router, private store: Store<State>, private translate: TranslateService) {}

  /**
   * Set application title with app name
   *
   * @param snapshot Current route snapshot
   */
  setTitle(snapshot: ActivatedRouteSnapshot) {
    let lastChild = snapshot;
    while (lastChild.children.length) {
      lastChild = lastChild.children[0];
    }
    const { title = null } = lastChild.data;

    if (title) {
      this.translate
        .get(title)
        .pipe(
          filter(translatedTitle => {
            return translatedTitle !== title && translatedTitle;
          })
        )
        .subscribe(translatedTitle => {
          this.title.setTitle(`${env.appName} - ${translatedTitle}`);
        });
    } else {
      this.title.setTitle(env.appName);
    }
  }
}
