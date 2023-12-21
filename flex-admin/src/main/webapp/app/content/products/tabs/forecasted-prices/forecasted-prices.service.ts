import { DownloadService, HttpService } from '@app/core';
import { DefaultParameters, FileDTO, Pageable } from '@app/shared/models';
import { ForecastedPricesDetailDTO, ForecastedPricesFileDTO } from './forecasted-prices';

import { ContentType } from '@app/shared/enums';
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { CUSTOM_HANDLE_ERROR_CONTEXT } from '@app/core/auth/auth.interceptor';

@Injectable()
export class ProductsForecastedPricessService extends HttpService {
  protected url = 'flex-server/api/admin/forecasted-prices';

  constructor(httpClient: HttpClient) {
    super(httpClient);
  }

  downloadTemplate(): void {
    this.get<FileDTO>(`${this.url}/template`).subscribe(({ fileName, base64StringData, fileExtension }: FileDTO) => {
      const [, contentType = ContentType.XLSX] = Object.entries(ContentType).find(([key]) => key === fileExtension) || [];

      DownloadService.saveFileWithParam(base64StringData, fileName, contentType, true);
    });
  }

  getForecastedPricesDetail(id: number): Observable<ForecastedPricesDetailDTO> {
    return this.get(`${this.url}/detail/${id}`);
  }

  loadCollection(parameters: DefaultParameters): Observable<Pageable<ForecastedPricesFileDTO>> {
    const { filters, ...params } = parameters;

    return this.getCollection<ForecastedPricesFileDTO>(this.url, {
      params: {
        ...params,
        ...filters,
      },
    });
  }

  import(data: FormData, force: boolean): Observable<void> {
    return this.post(`${this.url}`, data, {
      params: { force },
      context: CUSTOM_HANDLE_ERROR_CONTEXT,
    });
  }

  remove(id: number): Observable<void> {
    return this.delete(`${this.url}/${id}`);
  }
}
