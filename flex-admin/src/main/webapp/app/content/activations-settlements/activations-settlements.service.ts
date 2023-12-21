import { CUSTOM_HANDLE_ERROR_CONTEXT } from './../../core/auth/auth.interceptor';
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { AppToastrService, DownloadService, HttpService } from '@app/core';
import { DefaultParameters, FileDTO, Pageable } from '@app/shared/models';
import { ActivationsSettlementsDialogDTO, ActivationsSettlementsDTO } from '@app/shared/models/activations-settlements';
import { Observable, Subscription } from 'rxjs';
import * as moment from 'moment';
import { ContentType } from '@app/shared/enums';

@Injectable({
  providedIn: 'root',
})
export class ActivationsSettlementsService extends HttpService {
  protected url = 'flex-server/api/admin/settlements';

  constructor(httpClient: HttpClient, private toastr: AppToastrService) {
    super(httpClient);
  }

  loadCollection(parameters: DefaultParameters): Observable<Pageable<ActivationsSettlementsDTO>> {
    const { filters, ...params } = parameters;

    return this.getCollection<ActivationsSettlementsDTO>(this.url, {
      params: {
        ...params,
        ...filters,
      },
    });
  }

  getActivationSettlement(id: number): Observable<ActivationsSettlementsDTO> {
    return this.get(`${this.url}/${id}`);
  }

  getActivationSettlementMinVersion(id: number): Observable<ActivationsSettlementsDTO> {
    return this.get(`${this.url}/${id}/min`);
  }

  update(id: number, data: ActivationsSettlementsDialogDTO): Observable<void> {
    return this.put(`${this.url}/${id}`, data);
  }

  formatDeliveryDate(dateFrom: string, dateTo: string): string {
    const momentDateTo = moment(dateTo);

    const date = moment(dateFrom).format('DD/MM/YYYY');
    const fromTime = moment(dateFrom).format('HH:mm');
    const toTimeMinute = momentDateTo.format('mm');
    const toTimeHour = momentDateTo.isAfter(moment(dateFrom).endOf('day')) ? 24 : momentDateTo.format('HH');

    return `${date} ${fromTime} - ${toTimeHour}:${toTimeMinute}`;
  }

  import(data: FormData, force: boolean): Observable<void> {
    return this.post(`${this.url}`, data, {
      params: { force },
      context: CUSTOM_HANDLE_ERROR_CONTEXT,
    });
  }

  exportData(parameters: any): Subscription {
    return this.get<FileDTO>(`${this.url}/export`, {
      params: parameters,
    }).subscribe(({ fileName, base64StringData }: FileDTO) =>
      DownloadService.saveFileWithParam(base64StringData, fileName, ContentType.XLSX, true)
    );
  }
}
