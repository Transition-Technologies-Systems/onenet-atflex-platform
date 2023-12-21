import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { HTTP_INTERCEPTORS, HttpClient } from '@angular/common/http';
import { HttpClientTestingModule, HttpTestingController, TestRequest } from '@angular/common/http/testing';
import { Injectable } from '@angular/core';
import { TestBed } from '@angular/core/testing';

import { LoadingInterceptor } from './loading.interceptor';
import { LoadingService } from './loading.service';

@Injectable()
class DataService {
  apiEndpoint = 'http://localhost/api/test';

  constructor(private http: HttpClient) {}

  getData(ignoreLoader: boolean = false): Observable<any> {
    const headers = ignoreLoader ? { ignoreLoader: 'true' } : {};

    return this.http
      .get(this.apiEndpoint, {
        headers,
      })
      .pipe(catchError(() => throwError('Error')));
  }
}

describe('LoadingInterceptor', () => {
  let service: DataService;
  let loadingService: LoadingService;
  let httpMock: HttpTestingController;
  let interceptor: LoadingInterceptor;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        DataService,
        LoadingService,
        {
          provide: HTTP_INTERCEPTORS,
          useClass: LoadingInterceptor,
          multi: true,
        },
      ],
    });

    service = TestBed.inject(DataService);
    loadingService = TestBed.inject(LoadingService);
    httpMock = TestBed.inject(HttpTestingController);

    interceptor = new LoadingInterceptor(loadingService);
  });

  afterEach(() => {
    httpMock.verify();
  });

  describe('addRequest', () => {
    it('should add loading request', () => {
      spyOn(loadingService, 'addRequest');

      service.getData().subscribe();

      const req: TestRequest = httpMock.expectOne(service.apiEndpoint);
      req.flush([]);

      expect(loadingService.addRequest).toHaveBeenCalled();

      httpMock.verify();
    });

    it('should not add loading request when ignoreLoader is set', () => {
      spyOn(loadingService, 'addRequest');

      service.getData(true).subscribe();

      const req: TestRequest = httpMock.expectOne(service.apiEndpoint);
      req.flush([]);

      expect(loadingService.addRequest).not.toHaveBeenCalled();

      httpMock.verify();
    });
  });

  describe('removeRequest', () => {
    it('should remove loading request', () => {
      spyOn(loadingService, 'removeRequest');

      service.getData().subscribe({
        complete: () => {
          expect(loadingService.removeRequest).toHaveBeenCalled();
        },
      });

      const req: TestRequest = httpMock.expectOne(service.apiEndpoint);
      req.flush([]);

      httpMock.verify();
    });

    it('should remove loading request when request finish with error', () => {
      spyOn(loadingService, 'removeRequest');

      service.getData().subscribe({
        error: () => {
          expect(loadingService.removeRequest).toHaveBeenCalled();
        },
      });

      const req: TestRequest = httpMock.expectOne(service.apiEndpoint);
      req.flush({ errorMessage: 'Uh oh!' }, { status: 500, statusText: 'Server Error' });

      httpMock.verify();
    });
  });
});
