import { fakeAsync, TestBed, tick } from '@angular/core/testing';
import { Title } from '@angular/platform-browser';
import { ActivatedRouteSnapshot, ActivationEnd, Router } from '@angular/router';
import { environment as env } from '@env/environment';
import { Store } from '@ngrx/store';
import { provideMockStore } from '@ngrx/store/testing';
import { provideMockActivatedRouteSnapshot } from '@testing/mock-activated-route-snapshot';
import { provideMockRouter } from '@testing/mock-router';
import { provideMockTitle } from '@testing/mock-title';
import { MockTranslateService } from '@testing/mock-translate-service';

import { LanguageState } from '../language/language.models';
import { TitleService } from './title.service';

describe('TitleService', () => {
  let translateService;
  let titleService;
  let snapshot;
  let title;
  let store;
  let router;

  beforeEach(() => {
    const initialState = createState({ key: 'pl' });

    TestBed.configureTestingModule({
      providers: [provideMockActivatedRouteSnapshot(), provideMockStore({ initialState }), provideMockRouter(), provideMockTitle()],
    });

    title = TestBed.inject(Title);
    store = TestBed.inject(Store);
    router = TestBed.inject(Router);
    snapshot = TestBed.inject(ActivatedRouteSnapshot);

    translateService = new MockTranslateService();
    translateService.setTranslation('pl', { testTitle: 'Test' });

    titleService = new TitleService(title, router, store, translateService);
  });

  describe('setTitle', () => {
    it('should change page title', () => {
      snapshot.data = { title: 'testTitle' };
      titleService.setTitle(snapshot);
      expect(title.title).toBe(`${env.appName} - Test`);
    });
  });

  describe('changeApplicationTitle', () => {
    beforeEach(() => {
      spyOn(titleService, 'setTitle');
      store.setState({ language: 'en' });
    });

    it('should change application title when language change', fakeAsync(() => {
      titleService.changeApplicationTitle$.subscribe();
      tick();
      expect(titleService.setTitle).toHaveBeenCalled();
    }));

    it('should change application title when route change', fakeAsync(() => {
      const routerEvent = new ActivationEnd(router.routerState.snapshot);

      titleService.changeApplicationTitle$.subscribe();
      tick();
      router.events.next(routerEvent);
      tick();
      expect(titleService.setTitle).toHaveBeenCalledTimes(2);
    }));
  });
});

function createState(languageState: LanguageState) {
  return {
    language: languageState,
  };
}
