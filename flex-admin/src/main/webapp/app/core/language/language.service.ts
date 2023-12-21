import { MissingTranslationHandler, MissingTranslationHandlerParams, TranslateService } from '@ngx-translate/core';
import { NavigationEnd, Router } from '@angular/router';
import { Observable, Subscription, forkJoin, of } from 'rxjs';
import { State, getLanguageState } from '../core.state';
import { Store, select } from '@ngrx/store';
import { catchError, distinctUntilChanged, filter, map, switchMap, tap } from 'rxjs/operators';
import { isAuthenticated, pendingAuthentication } from '../auth/reducers';

import { BUILD } from '@env/build';
import { HttpClient } from '@angular/common/http';
import { HttpService } from '../http/http.service';
import { Injectable } from '@angular/core';
import { Language } from '@app/shared/enums';
import { PrimeNGConfig } from 'primeng/api';
import { languageChange } from './actions';

/**
 * Service to manage partials to the translations
 *
 * @example
 * this.subscription.add(LanguageService.init())
 */
@Injectable()
export class LanguageService extends HttpService {
  /**
   * Current translations
   */
  private translations: any = {};

  /**
   * Set with currently downloaded parts of the translation
   */
  private downloadedPartials: Set<string> = new Set<string>();
  /**
   * Initial partials to download
   */
  private initialPartials: Set<string> = new Set<string>(['enums', 'dictionaries', 'validation', 'menu', 'components', 'shared']);

  /**
   * Logged status
   */
  private isLogged = false;

  /**
   * Current used language
   */
  private currentLanguage: Language = 'en';

  /**
   * Subscriptions container
   */
  private subscriptions: Subscription = new Subscription();

  /**
   * Subscription for dynamic translate download
   */
  private dynamicTranslateSubscription: Subscription | undefined;

  /**
   * Subscribe logged status
   */
  private loggedStatus$ = this.store.pipe(
    select(pendingAuthentication),
    filter((pending: boolean) => !pending),
    switchMap(() => this.store.pipe(select(isAuthenticated))),
    tap(({ authenticated }) => {
      this.isLogged = !!authenticated;

      if (this.dynamicTranslateSubscription) {
        this.dynamicTranslateSubscription.unsubscribe();
      }

      this.dynamicTranslateSubscription = this.getDynamicTranslate(this.currentLanguage).subscribe();
    })
  );

  /**
   * Update application language
   */
  private setTranslateLanguage$: Observable<string> = this.store.pipe(
    select(getLanguageState),
    distinctUntilChanged(),
    tap(language => {
      const partials = Array.from(this.downloadedPartials).concat(Array.from(this.initialPartials));

      this.currentLanguage = language || 'en';

      this.translate.use(this.currentLanguage);

      this.getPartialsTranslation(partials, this.currentLanguage, () => {
        const translations = this.translations[this.currentLanguage];

        if (!!translations.components) {
          this.primengConfig.setTranslation(translations.components);
        }
      });
    })
  );

  constructor(
    http: HttpClient,
    private router: Router,
    private store: Store<State>,
    private translate: TranslateService,
    private primengConfig: PrimeNGConfig
  ) {
    super(http);
  }

  /**
   * Save user-chosen language to store and to user account (if authenticated).
   * @param key the language code.
   */
  changeUserLanguage(key: Language): void {
    this.store.dispatch(languageChange({ key }));
  }

  /**
   * Read current language from the store.
   */
  getCurrentLanguage$(): Observable<Language> {
    return this.store.select(getLanguageState);
  }

  /**
   * Download parts of translations
   *
   * @param partials List of parts names to be downloaded
   */
  getPartialsTranslation(partials: string[] = [], language?: string, callback?: () => void): Promise<boolean> {
    return new Promise(resolve => {
      if (partials.length === 0) {
        resolve(true);
      }

      const lang = language ? language : this.translate.currentLang;
      const dynamicTranslate = this.getDynamicTranslate(lang);
      const partialsRq = partials.map((key: string) => this.getPartial(lang, key));

      forkJoin([...partialsRq, dynamicTranslate]).subscribe({
        complete: () => {
          if (callback) {
            callback();
          }

          resolve(true);
        },
      });
    });
  }

  /**
   * Download translated definied by user
   *
   * @param lang Currently used language
   */
  getDynamicTranslate(lang: string): Observable<any> {
    if (!this.isLogged) {
      return of({});
    }

    return this.get(`flex-server/api/dictionary/translate/${lang}`).pipe(
      catchError(() => of({})),
      tap(response => this.setTranslations(lang, response))
    );
  }

  /**
   * Download part of translation and extend current translation in TranslateService
   *
   * @param key Currently used language
   * @param partial Name of the part to be downloaded from assets/i18n
   */
  getPartial(lang: string, partial: string): Observable<any> {
    this.downloadedPartials.add(partial);

    return this.http.get(`assets/i18n/${lang}/${partial}.json?v=${BUILD.timestamp}`).pipe(
      map(response => {
        this.setTranslations(lang, response);

        return response;
      })
    );
  }

  /**
   * Initializing function that retrieves default parts of translations
   */
  init(): Subscription {
    this.subscriptions.add(this.setTranslateLanguage$.subscribe());
    this.subscriptions.add(this.loggedStatus$.subscribe());

    return this.subscriptions;
  }

  postChangedLanguage(key: Language): Observable<any> {
    return this.post(`flex-server/api/users/change-lang/${key}`, null);
  }

  private setTranslations(lang: string, response: any): void {
    const translations = {
      ...this.translate.translations[lang],
      ...this.translations[lang],
    };

    this.translations[lang] = mergeObject(translations, response);

    this.translate.setTranslation(lang, this.translations[lang]);
  }
}

function mergeObject(object: any, data: any): object {
  if (!(data instanceof Object)) {
    return (object = data);
  }

  Object.entries(data).forEach(([key, values]) => {
    if (object[key]) {
      object[key] = mergeObject(object[key], values);
    } else {
      object[key] = values;
    }
  });

  return object;
}

@Injectable()
export class AppMissingTranslationHandler implements MissingTranslationHandler {
  handle(params: MissingTranslationHandlerParams): string {
    return params.key;
  }
}
