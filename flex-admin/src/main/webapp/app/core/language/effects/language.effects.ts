import * as languageActions from '../actions';

import { Actions, createEffect, ofType } from '@ngrx/effects';
import { Store, select } from '@ngrx/store';
import { tap, withLatestFrom } from 'rxjs/operators';

import { Injectable } from '@angular/core';
import { Language } from '@app/shared/enums';
import { LanguageService } from '../language.service';
import { LocalStorageService } from '../../storage/local-storage.service';
import { State } from '../../core.state';
import { getUserLang } from '../../auth/reducers';
import { updateUserField } from '@app/core/auth/actions';

export const LANGUAGE_KEY = 'LANGUAGE';

/**
 * Language effects
 */
@Injectable()
export class LanguageEffects {
  /**
   * Save current language to local storage
   */
  changeLanguage$ = createEffect(
    () =>
      this.actions$.pipe(
        ofType(languageActions.languageChange),
        withLatestFrom(this.store.pipe(select(getUserLang))),
        tap(([{ key, userLangKey }, langKey]) => {
          const userLang = (userLangKey || langKey || 'en').toLowerCase() as Language;

          if (this.localStorageService.getItem(LANGUAGE_KEY) !== key) {
            this.localStorageService.setItem(LANGUAGE_KEY, key);
          }

          if (userLang !== key) {
            this.service.postChangedLanguage(key).subscribe(() => {
              this.store.dispatch(updateUserField({ fieldName: 'langKey', newValue: key }));
            });
          }
        })
      ),
    { dispatch: false }
  );

  constructor(
    private actions$: Actions,
    private service: LanguageService,
    private localStorageService: LocalStorageService,
    private store: Store<State>
  ) {}
}
