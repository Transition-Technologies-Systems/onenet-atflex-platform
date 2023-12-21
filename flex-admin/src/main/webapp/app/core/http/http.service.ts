import { HttpClient, HttpParams, HttpResponse } from '@angular/common/http';
import { get, isNil, set, size } from 'lodash-es';

import { CustomHttpParamEncoder } from './http-param-encoder';
import { HttpOptions } from './http-options';
import { HttpRequestOptions } from './http-request-options';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Pageable } from '@app/shared/models';
import { environment as env } from '@env/environment';
import { isMoment } from 'moment';
import { map } from 'rxjs/operators';
import { moment } from 'polyfills';

/**
 * Helper to communication with HTTP
 */
@Injectable()
export abstract class HttpService {
  constructor(public http: HttpClient) {}

  /**
   * Request type GET
   *
   * @param url The endpoint URL
   * @param options The request options
   * @param customApiPrefix The custom api prefix
   */
  protected get<T>(url: string, options: HttpOptions = {}, customApiPrefix?: string): Observable<T> {
    const apiUrl = `${customApiPrefix ? customApiPrefix : env.SERVER_API_URL}${url}`;

    return this.http.get<T>(apiUrl, {
      ...options,
      params: this.toHttpParams(options.params),
    });
  }

  /**
   * Request type POST with pagination
   *
   * @param url The endpoint URL
   * @param options The request options
   * @param customApiPrefix The custom api prefix
   */
  protected getCollection<T>(
    url: string,
    options: HttpRequestOptions = {},
    method: string = 'GET',
    customApiPrefix?: string
  ): Observable<Pageable<T>> {
    const apiUrl = `${customApiPrefix ? customApiPrefix : env.SERVER_API_URL}${url}`;

    return this.http
      .request(method, apiUrl, {
        ...options,
        observe: 'response',
        body: this.formatPost(options.body),
        params: this.toHttpParams(options.params),
      })
      .pipe(
        map((response: HttpResponse<T[]>) => ({
          totalElements: parseInt(response.headers.get('X-Total-Count') || '0', 10),
          content: response.body || [],
        }))
      );
  }

  /**
   * Request type POST
   *
   * @param url The endpoint URL
   * @param data data to send
   * @param options The request options
   * @param customApiPrefix The custom api prefix
   */
  protected post<T>(url: string, data: any, options: HttpOptions = {}, customApiPrefix?: string): Observable<T> {
    const apiUrl = `${customApiPrefix ? customApiPrefix : env.SERVER_API_URL}${url}`;

    return this.http.post<T>(apiUrl, this.formatPost(data), {
      ...options,
      params: this.toHttpParams(options.params),
    });
  }

  /**
   * Request type PUT
   *
   * @param url The endpoint URL
   * @param data data to send
   * @param options The request options
   * @param customApiPrefix The custom api prefix
   */
  protected put<T>(url: string, data: any, options: HttpOptions = {}, customApiPrefix?: string): Observable<T> {
    const apiUrl = `${customApiPrefix ? customApiPrefix : env.SERVER_API_URL}${url}`;

    return this.http.put<T>(apiUrl, this.formatPost(data), {
      ...options,
      params: this.toHttpParams(options.params),
    });
  }

  /**
   * Request type DELETE
   *
   * @param url The endpoint URL
   * @param options The request options
   * @param customApiPrefix The custom api prefix
   */
  protected delete<T>(url: string, options: HttpOptions = {}, customApiPrefix?: string): Observable<T> {
    const apiUrl = `${customApiPrefix ? customApiPrefix : env.SERVER_API_URL}${url}`;

    return this.http.delete<T>(apiUrl, {
      ...options,
      params: this.toHttpParams(options.params),
    });
  }

  /**
   * Request to get any request type
   *
   * @param method The request method
   * @param url The endpoint URL
   * @param options The request options
   * @param customApiPrefix The custom api prefix
   */
  protected request(method: string, url: string, options: HttpRequestOptions = {}, customApiPrefix?: string): Observable<any> {
    const apiUrl = `${customApiPrefix ? customApiPrefix : env.SERVER_API_URL}${url}`;

    return this.http.request(method, apiUrl, {
      ...options,
      body: this.formatPost(options.body),
      params: this.toHttpParams(options.params),
    });
  }

  /**
   * Format data to date format with time
   *
   * @param data The data
   * @param paths The paths to format
   */
  protected formatDateTime(data: any, paths: string[]): any {
    paths.forEach((path: string) => {
      let value = get(data, path);

      if (isMoment(value)) {
        value = value.utc().format();
      } else if (value instanceof Date) {
        value = moment(value).utc().format();
      }

      set(data, path, value);
    });

    return data;
  }

  /**
   * Prepared http params
   *
   * @param params The object data
   */
  protected toHttpParams(
    data: object | undefined,
    paramsData: HttpParams = new HttpParams({ encoder: new CustomHttpParamEncoder() }),
    prefix: string | null = null
  ): HttpParams {
    if (!data) {
      return new HttpParams({ encoder: new CustomHttpParamEncoder() });
    }

    return Object.entries(data)
      .filter(([, value]) => !isNil(value) && (size(value) > 0 || !Array.isArray(value)))
      .reduce((params: HttpParams, [key, value]) => {
        const valueKey = prefix ? `${prefix}[${key}]` : key;

        if (Array.isArray(value) && key === 'sort') {
          value.forEach(paramValue => {
            params = params.append(valueKey, this.preparedHttpParamsValue(paramValue));
          });

          return params;
        } else if (typeof value === 'object' && !Array.isArray(value) && !isMoment(value)) {
          return this.toHttpParams(value, params, key);
        }

        return params.append(valueKey, this.preparedHttpParamsValue(value));
      }, paramsData);
  }

  /**
   * Prepared data to send
   *
   * @param data data to send
   */
  protected formatPost(data: any): any {
    if (!data) {
      return null;
    }

    if (data instanceof FormData) {
      return data;
    }

    if (Array.isArray(data)) {
      const formattedArray: any[] = [];

      data.forEach((value: any, index: number) => {
        formattedArray[index] = value;

        if (value instanceof Object) {
          formattedArray[index] = this.formatPost(value);
        }
      });

      return formattedArray;
    } else if (data instanceof Object) {
      const formattedData: any = {};

      Object.keys(data).forEach(key => {
        const value = data[key];
        if (isMoment(value) || value instanceof Date) {
          formattedData[key] = moment(value).format('YYYY-MM-DD');
        } else if (value instanceof Object) {
          formattedData[key] = this.formatPost(value);
        } else {
          formattedData[key] = value;
        }
      });

      return formattedData;
    }

    return data;
  }

  /**
   * Convert value to string for params
   *
   * @param value The value
   */
  private preparedHttpParamsValue(value: any): string {
    if (value instanceof String) {
      return value.toString();
    } else if (isMoment(value)) {
      return value.format('YYYY-MM-DD');
    } else if (value instanceof Date) {
      return moment(value).format('YYYY-MM-DD');
    } else if (Array.isArray(value)) {
      return value.join(',');
    }

    return value.toString();
  }
}
