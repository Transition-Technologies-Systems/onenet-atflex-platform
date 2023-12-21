import { MockProviders } from 'ng-mocks';
import { Observable, of } from 'rxjs';
import { Injectable } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { BUILD } from '@env/build';
import { Store } from '@ngrx/store';

import { State } from '../core.state';
import { LanguageService } from './language.service';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { HttpClient } from '@angular/common/http';

describe('LanguageService', () => {
  let httpService: HttpClient;
  let translateService: TranslateService;
  let languageService: LanguageService;
  let store: Store<State>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [TranslateModule.forRoot()],
      providers: [LanguageService, TranslateService, MockProviders(Store), MockProviders(HttpClient)],
    });

    httpService = TestBed.inject(HttpClient);
    languageService = TestBed.inject(LanguageService);
    translateService = TestBed.inject(TranslateService);
    store = TestBed.inject(Store);
    translateService.use('pl');
  });

  it('should be created', () => {
    expect(languageService).toBeTruthy();
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
      spyOn(httpService, 'get').and.callFake((url: any): any => {
        if (url === `assets/i18n/pl/test.json?v=${BUILD.timestamp}`) {
          return of(testFile);
        } else {
          return of(seconTestFile);
        }
      });

      languageService.getPartialsTranslation(['test', 'test2']);

      expect(translateService.translations).toEqual({
        pl: { ...testFile, ...seconTestFile },
      });
    });
  });

  describe('getPartial', () => {
    const i18nData = { title: 'application - title' };
    let expectedResult: any;

    beforeEach((done: DoneFn) => {
      spyOn(httpService, 'get').and.returnValue(of(i18nData));
      languageService.getPartial('pl', 'test').subscribe(result => {
        expectedResult = result;
        done();
      });
    });

    it('should get i18n file', () => {
      expect(httpService.get).toHaveBeenCalledWith(`assets/i18n/pl/test.json?v=${BUILD.timestamp}`);
    });

    it('should return i18n file', () => {
      expect(expectedResult).toEqual(i18nData);
    });

    it('should set translation data to translationService', () => {
      expect(translateService.translations).toEqual({ pl: i18nData });
    });
  });
});
