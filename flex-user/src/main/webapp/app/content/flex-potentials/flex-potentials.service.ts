import { AuthService, DownloadService, HttpService } from '@app/core';
import { Dictionary, FileDTO, FlexPotentialDTO, Pageable } from '@app/shared/models';
import { Observable, Subscription, from } from 'rxjs';
import { map, switchMap } from 'rxjs/operators';

import { ContentType } from '@app/shared/enums';
import { FlexPotentialsParameters } from './flex-potentials.store';
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

@Injectable()
export class FlexPotentialsService extends HttpService {
  protected url = 'api/user/flex-potentials';

  constructor(httpClient: HttpClient, private authService: AuthService) {
    super(httpClient);
  }

  downloadFile(id: number): void {
    this.get<FileDTO>(`${this.url}/files/${id}`).subscribe(({ fileName, base64StringData, fileExtension }: FileDTO) => {
      const [, contentType = ContentType.TXT] = Object.entries(ContentType).find(([key]) => key === fileExtension) || [];

      DownloadService.saveFileWithParam(base64StringData, fileName, contentType, true);
    });
  }

  exportXLSX(parameters: FlexPotentialsParameters | undefined, allData: boolean): Subscription {
    const type = allData ? 'all' : 'displayed-data';
    const { filters, ...restParameters } = parameters || {};

    return this.get<FileDTO>(`${this.url}/export/${type}`, {
      params: Object.assign(allData ? {} : filters, {
        ...restParameters,
      }),
    }).subscribe(({ fileName, base64StringData }: FileDTO) =>
      DownloadService.saveFileWithParam(base64StringData, fileName, ContentType.XLSX, true)
    );
  }

  getDerNames(isRegister: boolean): Observable<Dictionary[]> {
    const url = isRegister ? 'register/get-der-names' : 'get-der-names';

    return this.get<string[]>(`api/user/flex-potentials/${url}`).pipe(
      map(response => response.map((name: string) => ({ value: name, label: name })))
    );
  }

  getFlexPotential(id: number): Observable<FlexPotentialDTO> {
    return this.get<FlexPotentialDTO>(`${this.url}/${id}`);
  }

  getProducts(): Observable<Array<Dictionary & { minBidSize: number; maxBidSize: number }>> {
    return this.get<{ id: number; shortName: string; minBidSize: number; maxBidSize: number }[]>('api/user/products/get-all', {
      params: {
        'active.equals': true,
      },
    }).pipe(map(response => response.map(({ id, shortName, ...restData }) => ({ id, value: id, label: shortName, ...restData }))));
  }

  getUnits(flexPotentialId?: number): Observable<Dictionary<number>[]> {
    return from(this.authService.hasRole('ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED')).pipe(
      switchMap((hasRole: boolean) => {
        const url = !!hasRole ? 'api/user/units/get-all-for-fsp' : 'api/user/units/get-all';
        return this.get<{ id: number; name: string; sder: boolean; subportfolioName: string }[]>(url, {
          params: {
            flexPotentialId,
          },
        }).pipe(
          map(response =>
            response.map(({ id, name, sder, subportfolioName }) => ({
              id,
              value: id,
              label: hasRole ? `${name}${subportfolioName ? ` (${subportfolioName})` : ''}` : `${name}${sder ? '(SDER)' : ''}`,
            }))
          )
        );
      })
    );
  }

  import(data: FormData): Observable<void> {
    return this.post(`${this.url}/import/xlsx`, data);
  }

  loadCollection(parameters: FlexPotentialsParameters): Observable<Pageable<FlexPotentialDTO>> {
    const { filters, ...params } = parameters;

    return this.getCollection<FlexPotentialDTO>(this.url, {
      params: {
        ...params,
        ...filters,
      },
    });
  }

  remove(id: number): Observable<void> {
    return this.delete(`${this.url}/${id}`);
  }

  save(data: FlexPotentialDTO, files: File[]): Observable<void> {
    return from(this.formatData(data, files)).pipe(switchMap((formData: FormData) => this.post<any>(`${this.url}`, formData)));
  }

  update(id: number, data: FlexPotentialDTO, files: File[]): Observable<void> {
    return from(this.formatData(data, files)).pipe(switchMap((formData: FormData) => this.post<any>(`${this.url}/update`, formData)));
  }

  private formatData(data: FlexPotentialDTO, files: File[]): Promise<FormData> {
    const { ...form } = data;
    const formData = new FormData();

    return this.authService.hasRole('ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED').then((hasRole: boolean) => {
      const flexPotentialData = this.formatDateTime(
        {
          ...form,
          product: { id: form.productId },
        },
        ['validFrom', 'validTo']
      );

      formData.append('flexPotentialDTO', new Blob([JSON.stringify(flexPotentialData)], { type: 'application/json' }));

      if (files.length) {
        files.forEach((regularFile: File) => {
          formData.append('files', regularFile);
        });
      }

      return formData;
    });
  }
}
