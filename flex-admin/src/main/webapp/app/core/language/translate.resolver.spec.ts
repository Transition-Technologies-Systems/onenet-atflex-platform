import { InjectionToken } from '@angular/core';
import { TestBed, waitForAsync } from '@angular/core/testing';
import { ActivatedRouteSnapshot } from '@angular/router';
import { MockProvider, MockService } from 'ng-mocks';

import { LanguageService } from './language.service';
import { TranslateResolver } from './translate.resolver';

describe('TranslateResolver', () => {
  let resolver: TranslateResolver;
  let route: ActivatedRouteSnapshot;

  let languageService: LanguageService;

  const SERVICE_TOKEN = new InjectionToken<LanguageService>('SERVICE_TOKEN');

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      providers: [TranslateResolver, MockProvider(LanguageService)],
    });
  }));

  beforeEach(() => {
    languageService = TestBed.inject(LanguageService);
    resolver = TestBed.inject(TranslateResolver);
    route = new ActivatedRouteSnapshot();
  });

  describe('with the i18n data list in route snapshot', () => {
    const i18n = ['test'];

    beforeEach(() => {
      route.data = { i18n };
    });

    it('should be created', () => {
      expect(resolver).toBeTruthy();
    });

    it('should called getPartialsTranslation with a list of parts of the translations', () => {
      spyOn(languageService, 'getPartialsTranslation');
      resolver.resolve(route);
      expect(languageService.getPartialsTranslation).toHaveBeenCalledWith(i18n);
    });
  });

  describe('with the i18n data string in route snapshot', () => {
    const i18n = 'test';

    beforeEach(() => {
      route.data = { i18n };
    });

    it('should be created', () => {
      expect(resolver).toBeTruthy();
    });

    it('should called getPartialsTranslation with a list of parts of the translations', () => {
      spyOn(languageService, 'getPartialsTranslation');
      resolver.resolve(route);
      expect(languageService.getPartialsTranslation).toHaveBeenCalledWith([i18n]);
    });
  });

  describe('with empty the i18n data in route snapshot', () => {
    beforeEach(() => {
      route.data = {};
    });

    it('should be created', () => {
      expect(resolver).toBeTruthy();
    });

    it('should called getPartialsTranslation with empty partials data', () => {
      spyOn(languageService, 'getPartialsTranslation');
      resolver.resolve(route);
      expect(languageService.getPartialsTranslation).toHaveBeenCalledWith([]);
    });
  });
});
