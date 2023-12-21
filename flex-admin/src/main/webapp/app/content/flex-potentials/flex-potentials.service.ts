import { Dictionary, FileDTO, FlexPotentialDTO, Pageable } from '@app/shared/models';
import { AppToastrService, DownloadService, HttpService } from '@app/core';
import { Observable, Subscription } from 'rxjs';

import { ContentType } from '@app/shared/enums';
import { FlexPotentialsParameters } from './flex-potentials.store';
import { Injectable } from '@angular/core';
import { map } from 'rxjs/operators';
import { HttpClient } from '@angular/common/http';

@Injectable()
export class FlexPotentialsService extends HttpService {
  protected url = 'flex-server/api/admin/flex-potentials';

  constructor(http: HttpClient, private toastr: AppToastrService) {
    super(http);
  }

  downloadFile(id: number): void {
    this.get<FileDTO>(`flex-server/api/flex-potentials/files/${id}`).subscribe(({ fileName, base64StringData, fileExtension }: FileDTO) => {
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

    return this.get<string[]>(`flex-server/api/admin/flex-potentials/${url}`).pipe(
      map(response => response.map((name: string) => ({ value: name, label: name })))
    );
  }

  getFlexPotential(id: number): Observable<FlexPotentialDTO> {
    return this.get<FlexPotentialDTO>(`${this.url}/${id}`);
  }

  getProducts(): Observable<Array<Dictionary & { minBidSize: number; maxBidSize: number }>> {
    return this.get<{ id: number; shortName: string; minBidSize: number; maxBidSize: number }[]>('flex-server/api/admin/products/get-all', {
      params: {
        'active.equals': true,
      },
    }).pipe(map(response => response.map(({ id, shortName, ...restData }) => ({ value: id, label: shortName, ...restData }))));
  }

  getUnits(fspId?: number, isFspa?: boolean, flexPotentialId?: number): Observable<Dictionary<number>[]> {
    let request: Observable<{ id: number; name: string; sder: boolean; subportfolioName: string }[]>;

    if (!!isFspa) {
      request = this.get(`flex-server/api/admin/units/get-all-for-fsp`, {
        params: {
          fspaId: fspId,
          flexPotentialId,
        },
      });
    } else {
      request = this.get(`flex-server/api/admin/units/get-all`, {
        params: {
          'fspId.equals': fspId,
          flexPotentialId,
        },
      });
    }

    return request.pipe(
      map(response =>
        response.map(({ id, name, sder, subportfolioName }) => ({
          id,
          value: id,
          label: isFspa ? `${name}${subportfolioName ? ` (${subportfolioName})` : ''}` : `${name}${sder ? '(SDER)' : ''}`,
        }))
      )
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
    return this.post(`${this.url}`, this.formatData(data, files));
  }

  update(data: FlexPotentialDTO, files: File[]): Observable<void> {
    return this.post(`${this.url}/update`, this.formatData(data, files));
  }

  private formatData(data: FlexPotentialDTO, files: File[]): FormData {
    const { ...form } = data;
    const formData = new FormData();

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
  }
}
