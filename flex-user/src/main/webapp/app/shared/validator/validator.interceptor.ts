import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';

import { HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Injectable } from '@angular/core';

import { ValidatorService } from './validator.service';

@Injectable()
export class ValidatorInterceptor implements HttpInterceptor {
  constructor(private validatorService: ValidatorService) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(req).pipe(
      tap(
        () => {},
        error => this.handleError(error)
      )
    );
  }

  private handleError(response: HttpErrorResponse): void {
    if (response.status === 400) {
      this.validatorService.showValidators(response.error);
    }
  }
}
