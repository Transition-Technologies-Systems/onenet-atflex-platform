import { Dictionary, Pageable } from '@app/shared/models';
import { Observable, map } from 'rxjs';

import { DictionaryLangDto } from './dictionaries';
import { DictionaryPageParameters } from './dictionaries.store';
import { DictionaryType } from '@app/shared/enums';
import { HttpService } from '@app/core';
import { Injectable } from '@angular/core';

@Injectable()
export class DictionariesService extends HttpService {
  protected url = 'flex-server/dictionary/get-by-type';

  dictionaryType!: DictionaryType;

  getProducts(): Observable<Array<Dictionary & { minBidSize: number; maxBidSize: number }>> {
    return this.get<{ id: number; shortName: string; minBidSize: number; maxBidSize: number }[]>('flex-server/api/admin/products/get-all', {
      params: {
        'active.equals': true,
      },
    }).pipe(map(response => response.map(({ id, shortName, ...restData }) => ({ id, value: id, label: shortName, ...restData }))));
  }

  setDictionaryType(type: DictionaryType) {
    this.dictionaryType = type;
    switch (this.dictionaryType) {
      case DictionaryType.DER_TYPE:
        this.url = 'flex-server/api/der-types';
        break;
      case DictionaryType.SCHEDULING_UNIT_TYPE:
        this.url = 'flex-server/api/su-types';
        break;
      case DictionaryType.LOCALIZATION_TYPE:
        this.url = 'flex-server/api/admin/localization-types';
        break;
      case DictionaryType.KDM_MODEL:
        this.url = 'flex-agno/api/admin/kdm-models';
        break;
      default:
        this.url = 'flex-server/api/dictionary/get-by-type';
        break;
    }
  }

  loadCollection(parameters: DictionaryPageParameters): Observable<Pageable<DictionaryLangDto>> {
    const { type, filters, ...params } = parameters;
    return this.getCollection<DictionaryLangDto>(this.url, {
      params: {
        ...params,
        ...filters,
      },
    });
  }

  remove(id: number): Observable<void> {
    return this.delete(`${this.url}/${id}`);
  }

  save(data: any): Observable<void> {
    return this.post(`${this.url}`, data);
  }

  update(id: number, data: any): Observable<void> {
    return this.put(`${this.url}`, data);
  }

  getPositionDetails(id: number): Observable<DictionaryLangDto> {
    return this.get(`${this.url}/${id}`);
  }
}
