import { State } from '../core.state';
import { MockStore, provideMockStore } from '@ngrx/store/testing';
import { UserDTO } from '../../shared/models/user';
import { of } from 'rxjs';

import { HttpClient, HttpParams } from '@angular/common/http';
import { InjectionToken } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { environment as env } from '@env/environment';

import { HttpService } from '../http/http.service';
import { AuthService } from './auth.service';
import { MockProvider, MockProviders } from 'ng-mocks';
import { MemoizedSelector, Store } from '@ngrx/store';
import { LocalStorageService } from '../storage/local-storage.service';
import { Language } from '@app/shared/enums';
import { getLanguageState } from '../core.state';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

describe('AuthService', () => {
  let httpService: HttpService;
  let authService: AuthService;
  let store: MockStore<State>;
  let mockLanguageSelector: MemoizedSelector<State, Language>;
  let httpController: HttpTestingController;
  const initialState = { key: 'pl' };

  const AUTH_SERVICE_TOKEN = new InjectionToken<AuthService>('AUTH_SERVICE_TOKEN');
  const HTTP_SERVICE_TOKEN = new InjectionToken<HttpService>('HTTP_SERVICE_TOKEN');

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        HttpService,
        AuthService,
        MockProviders(HttpClient),
        MockProvider(LocalStorageService),
        provideMockStore({ initialState }),
        { provide: HTTP_SERVICE_TOKEN, useValue: HttpService },
        { provide: AUTH_SERVICE_TOKEN, useValue: AuthService },
      ],
    });

    store = TestBed.inject(Store<State>) as MockStore<State>;
    mockLanguageSelector = store.overrideSelector(getLanguageState, 'pl');
    httpController = TestBed.inject(HttpTestingController);
    authService = TestBed.inject(AuthService);
    httpService = TestBed.inject(HttpService);
  });

  describe('getUser', () => {
    let expectedResult: any;

    beforeEach((done: DoneFn) => {
      spyOn(httpService.http, 'get').and.returnValue(of({ id: 1, langKey: 'pl' } as UserDTO));
      mockLanguageSelector.setResult('pl');
      store.refreshState();
      authService.getUser('test', true).subscribe(result => {
        expectedResult = result;
        done();
      });
    });

    it('should reutrn jwt and user instance', () => {
      expect({ ...expectedResult[0] }).toEqual({
        jwt: 'test',
        initialization: true,
        user: { id: 1, langKey: 'pl' } as UserDTO,
      });
    });
  });
});
