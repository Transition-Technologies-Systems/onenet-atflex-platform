import { DownloadService, HttpService } from '@app/core';
import { DefaultParameters, FileDTO, Pageable } from '@app/shared/models';
import { SelfScheduleDetailDTO, SelfScheduleFileDTO } from './self-schedule';

import { ContentType } from '@app/shared/enums';
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { CUSTOM_HANDLE_ERROR_CONTEXT } from '@app/core/auth/auth.interceptor';

@Injectable()
export class UnitsSelfSchedulesService extends HttpService {
  protected url = 'flex-server/api/admin/self-schedule';

  constructor(httpClient: HttpClient) {
    super(httpClient);
  }

  downloadTemplate(): void {
    this.get<FileDTO>(`${this.url}/template`).subscribe(({ fileName, base64StringData, fileExtension }: FileDTO) => {
      const [, contentType = ContentType.XLSX] = Object.entries(ContentType).find(([key]) => key === fileExtension) || [];

      DownloadService.saveFileWithParam(base64StringData, fileName, contentType, true);
    });
  }

  getSelfScheduleDetail(id: number): Observable<SelfScheduleDetailDTO> {
    return this.get(`${this.url}/detail/${id}`);
  }

  loadCollection(parameters: DefaultParameters): Observable<Pageable<SelfScheduleFileDTO>> {
    const { filters, ...params } = parameters;

    return this.getCollection<SelfScheduleFileDTO>(this.url, {
      params: {
        ...params,
        ...filters,
      },
    });
  }

  import(data: FormData, force: boolean): Observable<void> {
    return this.post(`${this.url}`, data, {
      params: { force },
      context: CUSTOM_HANDLE_ERROR_CONTEXT
    });
  }

  remove(id: number): Observable<void> {
    return this.delete(`${this.url}/${id}`);
  }
}
