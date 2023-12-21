import { of } from 'rxjs';

import { HttpParams } from '@angular/common/http';
import { InjectionToken } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { UserDTO } from '@app/shared/model';
import { environment as env } from '@env/environment';
import { MockHttp } from '@testing/mock-http';

import { HttpService } from '../http/http.service';
import { AuthService } from './auth.service';

describe('AuthService', () => {
  let httpService: HttpService;
  let authService: AuthService;

  const AUTH_SERVICE_TOKEN = new InjectionToken<AuthService>('AUTH_SERVICE_TOKEN');
  const HTTP_SERVICE_TOKEN = new InjectionToken<HttpService>('HTTP_SERVICE_TOKEN');

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [{ provide: HTTP_SERVICE_TOKEN, newValue: new MockHttp() }, { provide: AUTH_SERVICE_TOKEN }],
    });

    authService = TestBed.inject(AUTH_SERVICE_TOKEN);
    httpService = TestBed.inject(HTTP_SERVICE_TOKEN);
  });

  describe('getUser', () => {
    let expectedResult;

    beforeEach(() => {
      spyOn(httpService.http, 'get').and.returnValue(of({ id: 1 } as UserDTO));
      authService.getUser('test', true).subscribe(result => (expectedResult = result));
    });

    it('should get user data with JWT auth', () => {
      expect(httpService.http.get).toHaveBeenCalledWith(`${env.SERVER_API_URL}api/authenticate`, {
        params: new HttpParams(),
        headers: {
          Authorization: 'Bearer test',
        },
      });
    });

    it('should reutrn jwt and user instance', () => {
      expect(expectedResult).toEqual({
        jwt: 'test',
        user: { id: 1 } as UserDTO,
      });
    });
  });
});
