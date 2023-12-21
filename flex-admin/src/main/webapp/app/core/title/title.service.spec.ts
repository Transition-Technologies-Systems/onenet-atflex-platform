import { State } from './../core.state';
import { fakeAsync, TestBed, tick } from '@angular/core/testing';
import { Title } from '@angular/platform-browser';
import { Router } from '@angular/router';
import { environment as env } from '@env/environment';
import { Store } from '@ngrx/store';
import { MockStore, provideMockStore } from '@ngrx/store/testing';
import { TranslateService } from '@ngx-translate/core';

import { TitleService } from './title.service';
import { Observable, of } from 'rxjs';
import { Injectable } from '@angular/core';

@Injectable()
export class TranslateServiceStub {
  public get<T>(key: T): Observable<T> {
    return of(key);
  }

  public instant<T>(key: T): T {
    return key;
  }
}

describe('TitleService', () => {
  let translateService: TranslateService;
  let titleService: TitleService;
  let title: Title;
  let store: MockStore<State>;
  let router: Router;

  beforeEach(() => {
    const initialState = { key: 'pl' };

    TestBed.configureTestingModule({
      providers: [TitleService, Title, provideMockStore({ initialState }), { provide: TranslateService, useClass: TranslateServiceStub }],
    });

    title = TestBed.inject(Title);
    titleService = TestBed.inject(TitleService);
    store = TestBed.inject(Store<State>) as MockStore<State>;
    router = TestBed.inject(Router);

    translateService = TestBed.inject(TranslateService);
  });

  describe('setTitle', () => {
    it('should change page title', () => {
      const mockTitle = 'testTitle';
      const snapshot: any = { data: mockTitle, children: [] };

      spyOn(translateService, 'get').withArgs(mockTitle).and.returnValue(of('Test'));
      snapshot.data = { title: 'testTitle' };
      titleService.setTitle(snapshot);
      expect(title.getTitle()).toBe(`${env.appName} - Test`);
    });
  });

  describe('changeApplicationTitle', () => {
    beforeEach(() => {
      spyOn(titleService, 'setTitle');
    });

    it('should change application title when language change', fakeAsync(() => {
      titleService.changeApplicationTitle$.subscribe();
      tick();
      expect(titleService.setTitle).toHaveBeenCalled();
    }));

    it('should change application title when route change', fakeAsync(() => {
      titleService.changeApplicationTitle$.subscribe();
      tick();
      expect(titleService.setTitle).toHaveBeenCalled();
    }));
  });
});
