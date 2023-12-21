import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { AppToastrService, DownloadService, HttpService } from '@app/core';
import { ContentType } from '@app/shared/enums';
import { DefaultParameters, FileDTO, Pageable } from '@app/shared/models';
import { Observable } from 'rxjs';
import { ProvideDataDTO } from './provide-data';

@Injectable()
export class ProvideDataService extends HttpService {
  protected url = 'flex-onenet/api/admin/provide-data';

  constructor(httpClient: HttpClient, private toastr: AppToastrService) {
    super(httpClient);
  }

  loadCollection(parameters: DefaultParameters): Observable<Pageable<ProvideDataDTO>> {
    const { filters, ...params } = parameters;

    return this.getCollection(this.url, {
      params: {
        ...params,
        ...filters,
      },
    });
  }

  downloadFile(id: number): void {
    this.get<FileDTO>(`${this.url}/${id}/file`).subscribe(({ fileName, base64StringData }: FileDTO) =>
      DownloadService.saveFileWithParam(base64StringData, fileName, ContentType.ZIP, true)
    );
  }
}
