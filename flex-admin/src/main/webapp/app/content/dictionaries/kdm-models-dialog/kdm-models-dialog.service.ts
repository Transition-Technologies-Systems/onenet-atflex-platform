import { HttpService } from '@app/core';
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { FormBuilder } from '@angular/forms';
import { Observable } from 'rxjs';
import { KdmTimestampModelDTO, KdmVerifyBody } from '../dictionaries';
import { CUSTOM_HANDLE_ERROR_CONTEXT } from '@app/core/auth/auth.interceptor';

@Injectable({
  providedIn: 'root',
})
export class KdmModelDialogService extends HttpService {
  protected url = 'flex-agno/api/admin/kdm-models/timestamps';

  constructor(httpClient: HttpClient, private fb: FormBuilder) {
    super(httpClient);
  }

  getModel(id: number): Observable<KdmTimestampModelDTO[]> {
    return this.get(`${this.url}`, { params: { kdmModelId: id } });
  }

  update(id: number, data: any): Observable<void> {
    return this.put(`${this.url}`, data);
  }

  save(data: any): Observable<void> {
    return this.post(`${this.url}`, data);
  }

  remove(id: number | undefined): Observable<void> {
    return this.delete(`${this.url}/${id}`);
  }

  uploadModel(body: KdmTimestampModelDTO[]): Observable<void> {
    return this.put<any>(this.url, this.formatData(body));
  }

  verifyModel(data: KdmVerifyBody): Observable<void> {
    return this.post<void>(`${this.url}/verify`, this.formatVerifyData(data), { context: CUSTOM_HANDLE_ERROR_CONTEXT });
  }

  getInitialData(kdmModelId: number): KdmTimestampModelDTO[] {
    const data: KdmTimestampModelDTO[] = [
      { timestamp: '1', fileName: null, kdmModelId, fileDTO: null },
      { timestamp: '2', fileName: null, kdmModelId, fileDTO: null },
      { timestamp: '3', fileName: null, kdmModelId, fileDTO: null },
      { timestamp: '4', fileName: null, kdmModelId, fileDTO: null },
      { timestamp: '5', fileName: null, kdmModelId, fileDTO: null },
      { timestamp: '6', fileName: null, kdmModelId, fileDTO: null },
      { timestamp: '7', fileName: null, kdmModelId, fileDTO: null },
      { timestamp: '8', fileName: null, kdmModelId, fileDTO: null },
      { timestamp: '9', fileName: null, kdmModelId, fileDTO: null },
      { timestamp: '10', fileName: null, kdmModelId, fileDTO: null },
      { timestamp: '11', fileName: null, kdmModelId, fileDTO: null },
      { timestamp: '12', fileName: null, kdmModelId, fileDTO: null },
      { timestamp: '13', fileName: null, kdmModelId, fileDTO: null },
      { timestamp: '14', fileName: null, kdmModelId, fileDTO: null },
      { timestamp: '15', fileName: null, kdmModelId, fileDTO: null },
      { timestamp: '16', fileName: null, kdmModelId, fileDTO: null },
      { timestamp: '17', fileName: null, kdmModelId, fileDTO: null },
      { timestamp: '18', fileName: null, kdmModelId, fileDTO: null },
      { timestamp: '19', fileName: null, kdmModelId, fileDTO: null },
      { timestamp: '20', fileName: null, kdmModelId, fileDTO: null },
      { timestamp: '21', fileName: null, kdmModelId, fileDTO: null },
      { timestamp: '22', fileName: null, kdmModelId, fileDTO: null },
      { timestamp: '23', fileName: null, kdmModelId, fileDTO: null },
      { timestamp: '24', fileName: null, kdmModelId, fileDTO: null },
      { timestamp: '2a', fileName: null, kdmModelId, fileDTO: null },
    ];
    return data;
  }

  private formatVerifyData({ file, kdmFileId, timestamp, kdmModelId }: KdmVerifyBody): FormData {
    const formData = new FormData();
    formData.append('file', file);
    if (kdmFileId) {
      formData.append('kdmFileId', kdmFileId as any);
    }
    formData.append('timestamp', timestamp);
    formData.append('kdmModelId', kdmModelId as any);
    return formData;
  }

  private formatData(data: KdmTimestampModelDTO[]): FormData {
    const formData = new FormData();
    formData.append(`kdmModelId`, data[0].kdmModelId.toString());
    data.forEach(item => {
      if (item.fileDTO) {
        formData.append(`file${item.timestamp}`, item.fileDTO);
      } else {
        formData.append(`file${item.timestamp}`, item.fileName ? '' : (null as any));
      }
      formData.append(`id${item.timestamp}`, item && item.id ? item.id.toString() : (null as any));
    });
    return formData;
  }
}
