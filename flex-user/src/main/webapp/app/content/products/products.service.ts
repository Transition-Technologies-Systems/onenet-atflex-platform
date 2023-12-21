import { ContentType, Role } from '@app/shared/enums';
import { DefaultParameters, Dictionary, FileDTO, Pageable, ProductDTO } from '@app/shared/models';
import { DownloadService, HttpService } from '@app/core';
import { Observable, Subscription } from 'rxjs';

import { Injectable } from '@angular/core';
import { map } from 'rxjs/operators';
import { HttpClient } from '@angular/common/http';

@Injectable()
export class ProductsService extends HttpService {
  protected url = 'api/user/products';

  constructor(http: HttpClient, ) {
    super(http);
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

  getUsers(): Observable<Dictionary[]> {
    return this.get<{ id: number; login: string; roles: Role[] }[]>('api/user/products/users/get-pso-sso').pipe(
      map(response =>
        response.map(({ id, login, roles }) => {
          const isTSO = roles.includes(Role.ROLE_TRANSMISSION_SYSTEM_OPERATOR);

          return { id, value: id, label: `${login} (${isTSO ? 'TSO' : 'DSO'})` };
        })
      )
    );
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
}
