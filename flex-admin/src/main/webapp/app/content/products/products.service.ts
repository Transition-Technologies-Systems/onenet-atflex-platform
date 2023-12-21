import { ContentType, Role } from '@app/shared/enums';
import { DefaultParameters, Dictionary, FileDTO, Pageable, ProductDTO } from '@app/shared/models';
import { DownloadService, HttpService } from '@app/core';
import { Observable, Subscription } from 'rxjs';

import { HttpClient,  } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Tab } from './product';
import { TranslateService } from '@ngx-translate/core';
import { map } from 'rxjs/operators';

@Injectable()
export class ProductsService extends HttpService {
  protected url = 'flex-server/api/admin/products';

  constructor(httpClient: HttpClient, private translate: TranslateService) {
    super(httpClient);
  }

  downloadFile(id: number): void {
    this.get<FileDTO>(`${this.url}/files/${id}`).subscribe(({ fileName, base64StringData, fileExtension }: FileDTO) => {
      const [, contentType = ContentType.TXT] = Object.entries(ContentType).find(([key]) => key === fileExtension) || [];

      DownloadService.saveFileWithParam(base64StringData, fileName, contentType, true);
    });
  }

  exportXLSX(parameters: DefaultParameters | undefined, allData: boolean): Subscription {
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

  getProduct(id: number): Observable<ProductDTO> {
    return this.get(`${this.url}/${id}`);
  }

  getProducts(): Observable<Array<Dictionary & { minBidSize: number; maxBidSize: number }>> {
    return this.get<{ id: number; shortName: string; minBidSize: number; maxBidSize: number }[]>('flex-server/api/admin/products/get-all', {
      params: {
        'active.in': 'true,false',
      },
    }).pipe(map(response => response.map(({ id, shortName, ...restData }) => ({ id, value: id, label: shortName, ...restData }))));
  }

  getUsers(): Observable<Dictionary[]> {
    return this.get<{ id: number; login: string; roles: Role[] }[]>('flex-server/api/admin/products/users/get-pso-sso').pipe(
      map(response =>
        response.map(({ id, login, roles }) => {
          const isTSO = roles.includes(Role.ROLE_TRANSMISSION_SYSTEM_OPERATOR);

          return { id, value: id, label: `${login} (${isTSO ? 'TSO' : 'DSO'})` };
        })
      )
    );
  }

  getTabs(): Tab[] {
    return [
      {
        label: this.translate.instant('products.tabs.list'),
        type: 'list',
      },
      {
        label: this.translate.instant('products.tabs.forecastedPrices'),
        type: 'forecasted-prices',
      },
    ];
  }

  loadCollection(parameters: DefaultParameters): Observable<Pageable<ProductDTO>> {
    const { filters, ...params } = parameters;

    return this.getCollection<ProductDTO>(this.url, {
      params: {
        ...params,
        ...filters,
      },
    });
  }

  remove(id: number): Observable<void> {
    return this.delete(`${this.url}/${id}`);
  }

  save(data: ProductDTO, regularFiles: File[]): Observable<void> {
    return this.post(`${this.url}`, this.formatData(data, regularFiles));
  }

  update(data: ProductDTO, regularFiles: File[]): Observable<void> {
    return this.post(`${this.url}/update`, this.formatData(data, regularFiles));
  }

  private formatData(data: ProductDTO, regularFiles: File[]): FormData {
    const { ...form } = data;
    const formData = new FormData();

    const productData = this.formatDateTime(form, ['validFrom', 'validTo']);

    formData.append('productDTO', new Blob([JSON.stringify(productData)], { type: 'application/json' }));

    if (regularFiles.length) {
      regularFiles.forEach((regularFile: File) => {
        formData.append('regularFiles', regularFile);
      });
    }

    return formData;
  }
}
