import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { AppToastrService, DownloadService, HttpService } from '@app/core';
import { ContentType } from '@app/shared/enums';
import { DefaultParameters, FileDTO, Pageable } from '@app/shared/models';
import { Observable } from 'rxjs';
import { OfferedServicesDTO } from './offered-services';

@Injectable()
export class OfferedServicesService extends HttpService {
  protected url = 'flex-onenet/api/admin/offered-services';

  constructor(httpClient: HttpClient, private toastr: AppToastrService) {
    super(httpClient);
  }

  loadCollection(parameters: DefaultParameters): Observable<Pageable<OfferedServicesDTO>> {
    const { filters, ...params } = parameters;

    return this.getCollection(this.url, {
      params: {
        ...params,
        ...filters,
      },
    });
  }

  downloadFile(id: number, isSchema: boolean): void {
    let currentUrl = isSchema ? `${this.url}/download/file-schema-sample/${id}` : `${this.url}/download/file-schema/${id}`;
    this.get<FileDTO>(currentUrl).subscribe(({ fileName, base64StringData }: FileDTO) =>
      DownloadService.saveFileWithParam(base64StringData, fileName, ContentType.ZIP, true)
    );
  }
}
