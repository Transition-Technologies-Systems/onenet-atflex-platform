import { ContentType, Role } from '@app/shared/enums';
import { DownloadService, HttpService } from '@app/core';
import { FileDTO, FspDTO, Pageable } from '@app/shared/models';
import { Observable, Subscription, } from 'rxjs';

import { FspsParameters } from './fsps.store';
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable()
export class FspsService extends HttpService {
  protected url = 'flex-server/api/fsps';

  constructor(http: HttpClient) {
    super(http);
  }

  getFsp(id: number): Observable<FspDTO> {
    return this.get(`${this.url}/${id}`);
  }

  exportXLSX(parameters: FspsParameters | undefined, allData: boolean): Subscription {
    const type = allData ? 'all' : 'displayed-data';
    const { filters, bsp, ...params } = parameters || {};

    return this.get<FileDTO>(`${this.url}/export/${type}`, {
      params: Object.assign(
        {
          ...params,
          'role.in': bsp
            ? [Role.ROLE_BALANCING_SERVICE_PROVIDER]
            : [Role.ROLE_FLEX_SERVICE_PROVIDER, Role.ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED],
        },
        filters
      ),
    }).subscribe(({ fileName, base64StringData }: FileDTO) =>
      DownloadService.saveFileWithParam(base64StringData, fileName, ContentType.XLSX, true)
    );
  }

  loadCollection(parameters: FspsParameters): Observable<Pageable<FspDTO>> {
    const { filters, bsp, ...params } = parameters;

    return this.getCollection<FspDTO>(this.url, {
      params: {
        ...params,
        'role.in': bsp
          ? [Role.ROLE_BALANCING_SERVICE_PROVIDER]
          : [Role.ROLE_FLEX_SERVICE_PROVIDER, Role.ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED],
        ...filters,
      },
    });
  }

  remove(id: number): Observable<void> {
    return this.delete(`${this.url}/${id}`);
  }

  update(data: FspDTO): Observable<void> {
    const form = this.formatDateTime(data, ['validFrom', 'validTo']);

    return this.put(`${this.url}`, {
      ...form,
      representative: {
        ...form.representative,
        phoneNumber: form.representative?.phoneNumber?.e164Number,
      },
    });
  }
}
