import { Observable } from 'rxjs';
import { finalize } from 'rxjs/operators';

import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Injectable } from '@angular/core';

import { LoadingService } from './loading.service';

/**
 * Loading interceptor, handle request and show loading
 */
@Injectable()
export class LoadingInterceptor implements HttpInterceptor {
  constructor(private loadignService: LoadingService) {}

  /**
   * Interceptor to handle request
   */
  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    if (!this.isIgnoreRequest(req)) {
      this.loadignService.addRequest();

      return next.handle(req).pipe(finalize(() => this.loadignService.removeRequest()));
    }

    return next.handle(req);
  }

  /**
   * Check if the request is ignored
   */
  private isIgnoreRequest(req: HttpRequest<any>) {
    return req.headers.get('ignoreLoader') === 'true';
  }
}
