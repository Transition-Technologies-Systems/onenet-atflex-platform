import { Observable } from 'rxjs';

import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { DownloadService, HttpService } from '@app/core';
import { ContentType } from '@app/shared/enums';
import { FileDTO } from '@app/shared/models';

import { DocumentDTO } from './manuals.models';

@Injectable()
export class ManualsService extends HttpService {
  constructor(httpClient: HttpClient) {
    super(httpClient);
  }

  getManualFilesList(): Observable<DocumentDTO[]> {
    return this.get<DocumentDTO[]>('api/users/manual');
  }

  getManualFile(id: string): Observable<FileDTO> {
    return this.get<FileDTO>(`api/users/manual/${id}`);
  }

  downloadDoc = (event: any) => {
    this.getManualFile(event.item?.label).subscribe(({ fileName, base64StringData, fileExtension }: FileDTO) => {
      const [ext, contentType = ContentType.TXT] = Object.entries(ContentType).find(([key]) => key === fileExtension) || [];
      DownloadService.saveFileWithParam(base64StringData, fileName, contentType, true);
    });
  };
}
