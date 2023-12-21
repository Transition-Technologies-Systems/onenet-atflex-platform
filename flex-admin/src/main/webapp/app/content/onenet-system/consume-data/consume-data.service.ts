import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { AppToastrService, DownloadService, HttpService } from '@app/core';
import { ContentType } from '@app/shared/enums';
import { DefaultParameters, FileDTO, Pageable } from '@app/shared/models';
import { Observable } from 'rxjs';
import { ConsumeDataDTO } from './consume-data';

@Injectable()
export class ConsumeDataService extends HttpService {
  protected url = 'flex-onenet/api/admin/consume-data';

  constructor(httpClient: HttpClient, private toastr: AppToastrService) {
    super(httpClient);
  }

  loadCollection(parameters: DefaultParameters): Observable<Pageable<ConsumeDataDTO>> {
    const { filters, ...params } = parameters;

    return this.getCollection(this.url, {
      params: {
        ...params,
        ...filters,
      },
    });
  }

  downloadFile(onsId: string): void {
    this.get<FileDTO>(`${this.url}/download/file/${onsId}`).subscribe(({ fileName, base64StringData }: FileDTO) =>
      DownloadService.saveFileWithParam(base64StringData, fileName, ContentType.ZIP, true)
    );
  }
}
