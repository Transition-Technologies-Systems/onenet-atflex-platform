import { map, Observable, Subscription, tap } from 'rxjs';
import { Injectable } from '@angular/core';
import { DownloadService, HttpService } from '@app/core';
import { KpiGenerateDto } from './kpi.store';
import { DefaultParameters, Dictionary, FileDTO, Pageable } from '@app/shared/models';
import { KpiDTO } from '@app/shared/models/kpi';
import { ContentType } from '@app/shared/enums';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root',
})
export class KpiService extends HttpService {
  protected url = 'flex-server/api/kpis';

  constructor(httpClient: HttpClient) {
    super(httpClient);
  }

  loadCollection(parameters: DefaultParameters): Observable<Pageable<KpiDTO>> {
    const { filters, ...params } = parameters;

    return this.getCollection<KpiDTO>(this.url, {
      params: {
        ...params,
        ...filters,
      },
    });
  }

  generateKpi(body: KpiGenerateDto): Observable<void> {
    const { type, dateFrom, dateTo } = body || {};

    return this.post<FileDTO>(`${this.url}/${type}`, null, {
      params: {
        dateFrom,
        dateTo,
      },
    }).pipe(
      tap(({ fileName, base64StringData }: any): any =>
        DownloadService.saveFileWithParam(base64StringData, fileName, ContentType.XLSX, true)
      )
    );
  }

  regenerateKpi(id: number): Subscription {
    return this.post<FileDTO>(`${this.url}/${id}/regenerate`, null).subscribe(({ fileName, base64StringData }: any): any =>
      DownloadService.saveFileWithParam(base64StringData, fileName, ContentType.XLSX, true)
    );
  }

  getKpiTypes(): Observable<Dictionary[]> {
    return this.get<Dictionary[]>(`${this.url}/types`).pipe(
      map(response =>
        response.map(({ name, filterDate }) => {
          return {
            label: `kpi.types.${name}`,
            value: name,
            filterDate,
          };
        })
      )
    );
  }
}
