import { HttpContext, HttpHeaders, HttpParams } from '@angular/common/http';

/**
 * Http options form method GET/POST/PUT/DELETE
 */
export class HttpOptions {
  headers?:
    | HttpHeaders
    | {
        [header: string]: string | string[];
      };
  observe?: any;
  params?:
    | HttpParams
    | {
        [param: string]: any | any[];
      };
  reportProgress?: boolean;
  responseType?: 'json';
  withCredentials?: boolean;
  context?: HttpContext;
}
