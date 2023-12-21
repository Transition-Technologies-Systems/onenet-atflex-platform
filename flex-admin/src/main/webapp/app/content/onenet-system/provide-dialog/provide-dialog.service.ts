import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { HttpService } from '@app/core';
import { Dictionary } from '@app/shared/models';
import { map, Observable } from 'rxjs';
import { ProvideDialogDictItemDTO, ProvideDialogDTO } from './provide-dialog';

@Injectable({
  providedIn: 'root',
})
export class ProvideDialogService extends HttpService {
  constructor(httpClient: HttpClient) {
    super(httpClient);
  }

  getServicesDictionary(): Observable<Dictionary[]> {
    return this.get<ProvideDialogDictItemDTO[]>('flex-onenet/api/admin/offered-services/get-all-min').pipe(
      map(response => response.map(({ onenetId, title }) => ({ value: onenetId, label: `${title} (${onenetId})` })))
    );
  }

  sendData(data: ProvideDialogDTO): Observable<void> {
    return this.post('flex-onenet/api/admin/provide-data', this.formatData(data));
  }

  formatData(data: ProvideDialogDTO): FormData {
    let formData = new FormData();
    formData.append('title', data.title);
    formData.append('description', data.description);
    formData.append('filename', data.filename ? data.filename : '');
    formData.append('file', data.file, data.filename ? data.filename : '');
    formData.append('dataOfferingId', data.dataOfferingId);
    formData.append('code', data.code);
    return formData;
  }
}
