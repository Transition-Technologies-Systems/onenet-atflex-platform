import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, Resolve } from '@angular/router';

import { CoreModule } from '../core.module';
import { LanguageService } from './language.service';

/**
 * Resolve parts translations for route
 */
@Injectable({
  providedIn: CoreModule,
})
export class TranslateResolver implements Resolve<boolean> {
  constructor(private language: LanguageService) {}

  /**
   * Resolve parts translation for current route
   *
   * @param route Current route snapshot
   */
  resolve(route: ActivatedRouteSnapshot): Promise<boolean> {
    return this.language.getPartialsTranslation(this.getRouteTranslatePartials(route));
  }

  /**
   * Get the names of the translation parts from the current route snapshot
   *
   * @param route Current route snapshot
   */
  private getRouteTranslatePartials(route: ActivatedRouteSnapshot): string[] {
    const i18n = route.data.i18n || [];

    return Array.isArray(i18n) ? i18n : [i18n];
  }
}
