import { Observable, of } from 'rxjs';

import { fakeAsync, TestBed, tick } from '@angular/core/testing';
import { BUILD } from '@env/build';
import { Store } from '@ngrx/store';
import { MockStore, provideMockStore } from '@ngrx/store/testing';
import { MockHttp } from '@testing/mock-http';
import { MockTranslateService } from '@testing/mock-translate-service';

import { State } from '../core.state';
import { LanguageService } from './language.service';

describe('LanguageService', () => {
  let httpService;
  let translateService;
  let store: MockStore<State>;
  let languageService: LanguageService;

  beforeEach(() => {
    const initialState = {};

    TestBed.configureTestingModule({
      providers: [provideMockStore({ initialState })],
    });

    store = TestBed.inject(Store);
    httpService = new MockHttp();
    translateService = new MockTranslateService();

    languageService = new LanguageService(translateService, store, httpService);
  });

  it('should be created', () => {
    expect(languageService).toBeTruthy();
  });

  describe('init', () => {
    it('should called getPartialsTranslation with initial partials', () => {
      spyOn(languageService, 'getPartialsTranslation');
      languageService.init();
      expect(languageService.getPartialsTranslation).toHaveBeenCalled();
    });
  });

  describe('getPartialsTranslation', () => {
    const testFile = { title: 'test' };
    const seconTestFile = { send: 'send' };

    it('should not called getPartial when have empty partials', () => {
      spyOn(languageService, 'getPartial');
      languageService.getPartialsTranslation([]);
      expect(languageService.getPartial).not.toHaveBeenCalled();
    });

    it('should merge i18n data', () => {
      translateService.currentTranslation = {};

      spyOn(httpService, 'get').and.callFake(url => {
        if (url === `assets/i18n/pl/test.json?v=${BUILD.timestamp}`) {
          return of(testFile);
        } else {
          return of(seconTestFile);
        }
      });

      languageService.getPartialsTranslation(['test', 'test2']);

      expect(translateService.currentTranslation).toEqual({
        pl: { ...testFile, ...seconTestFile },
      });
    });
  });

  describe('getPartial', () => {
    const i18nData = { title: 'application - title' };
    let expectedResult;

    beforeEach(() => {
      translateService.currentTranslation = {};
      spyOn(httpService, 'get').and.returnValue(of(i18nData));
      languageService.getPartial('pl', 'test').subscribe(result => (expectedResult = result));
    });

    it('should get i18n file', () => {
      expect(httpService.get).toHaveBeenCalledWith(`assets/i18n/pl/test.json?v=${BUILD.timestamp}`);
    });

    it('should return i18n file', () => {
      expect(expectedResult).toEqual(i18nData);
    });

    it('should set translation data to translationService', () => {
      expect(translateService.currentTranslation).toEqual({ pl: i18nData });
    });
  });

  describe('setTranslateLanguage', () => {
    beforeEach(() => {
      spyOn(translateService, 'use');
      store.setState({ language: 'en' } as State);
    });

    it('should change translate key in TranslateService', fakeAsync(() => {
      spyOn(httpService, 'get').and.returnValue(
        new Observable(observer => {
          observer.next({});
          observer.complete();
        })
      );

      languageService.setTranslateLanguage$.subscribe();
      tick();
      expect(translateService.use).toHaveBeenCalledWith('en');
    }));
  });
});
