import { HttpService } from '@app/core';

import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { MyOffersDTO } from './my-offers';
import { Observable } from 'rxjs';
import { DefaultParameters, Pageable } from '@app/shared/models';

@Injectable()
export class MyOffersService extends HttpService {
  protected url = 'api/user/auctions/offers/view';

  constructor(httpClient: HttpClient) {
    super(httpClient);
  }

  loadCollection(parameters: DefaultParameters): Observable<Pageable<MyOffersDTO>> {
    const { filters, ...params } = parameters;

    return this.getCollection(this.url, {
      params: {
        ...params,
        ...filters,
      },
    });
  }
}
