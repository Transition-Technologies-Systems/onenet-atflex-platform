import { Observable, catchError, tap } from 'rxjs';

import {
  HttpContext,
  HttpContextToken,
  HttpErrorResponse,
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest,
} from '@angular/common/http';
import { Injectable } from '@angular/core';
import * as AuthActions from '@app/core/auth/actions';
import { Store } from '@ngrx/store';

import { State } from '../core.state';
import { AppToastrService } from '../toastr/toastr.service';
import { AuthState } from './auth.models';
import { AuthService } from './auth.service';

export const HANDLE_ERROR_TOKEN = new HttpContextToken(() => true);
export const CUSTOM_HANDLE_ERROR_CONTEXT = new HttpContext().set(HANDLE_ERROR_TOKEN, false);
/**
 * Auth interceptor, handle request and add Authentication header
 */
@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(private store: Store<State>, private toastr: AppToastrService, private authService: AuthService) {}

  /**
   * Interceptor to handle request
   */
  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const authState: AuthState = this.authService.getToken();
    const headers: { [name: string]: string | string[] } = {};

    if (authState && !!authState.jwt && req.url.indexOf('authenticate') === -1) {
      headers.Authorization = `Bearer ${authState.jwt}`;
    }

    const request = req.clone({
      setHeaders: {
        ...headers,
        Gateway: 'FLEX-ADMIN',
      },
    });

    return next.handle(request).pipe(
      tap(() => {}),
      catchError((error: HttpErrorResponse) => {
        this.handleError(error, request);
        throw error;
      })
    );
  }

  private handleError(error: HttpErrorResponse, rq: HttpRequest<any>): void {
    const { url, context } = rq;

    if (error instanceof HttpErrorResponse) {
      switch (error.status) {
        case 400:
          if (context.get(HANDLE_ERROR_TOKEN) && error.error?.errorKey) {
            if (error.error?.errorKey === 'error.chat.alreadyExists') {
              this.toastr.warning(error.error?.errorKey);
            } else {
              this.toastr.error(error.error?.errorKey);
            }
          }
          break;
        case 401:
        case 403:
          if (url.includes('account')) {
            this.store.dispatch(AuthActions.invalidJWT());
          }
          break;
        case 409:
          switch (error.error?.message) {
            case 'error.objectModifiedByAnotherUser':
              this.toastr.warning('error.objectModifiedByAnotherUser');
              break;
          }
          break;
      }
    }
  }

  private camelCase(value: string): string {
    return value.replace(/_([a-z])/g, g => g[1].toUpperCase());
  }
}
