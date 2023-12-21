import { DefaultParameters, Dictionary, Pageable } from '@app/shared/models';
import { Observable, map } from 'rxjs';

import { HttpService } from '@app/core';
import { Injectable } from '@angular/core';
import { SchedulingUnitTypeDTO } from './types';

@Injectable()
export class SchedulingUnitsTypesService extends HttpService {
  protected url = 'api/user/su-types';

  getProducts(): Observable<Dictionary[]> {
    return this.get<{ id: number; shortName: string }[]>('api/user/products/get-all', {
      params: {
        'active.equals': true,
      },
    }).pipe(map(response => response.map(({ id, shortName }) => ({ id, value: id, label: shortName }))));
  }

  loadCollection(parameters: DefaultParameters): Observable<Pageable<SchedulingUnitTypeDTO>> {
    const { filters, ...params } = parameters;

    return this.getCollection<SchedulingUnitTypeDTO>(this.url, {
      params: {
        ...params,
        ...filters,
      },
    });
  }
}
