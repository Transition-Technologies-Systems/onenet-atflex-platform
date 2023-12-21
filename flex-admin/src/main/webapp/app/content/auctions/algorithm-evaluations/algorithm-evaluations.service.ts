import { AlgorithmEvaluationDTO, AlgorithmEvaluationLogDTO } from './algorithm-evaluation';
import { AppToastrService, DownloadService, HttpService } from '@app/core';
import { DefaultParameters, Dictionary, FileDTO, Pageable } from '@app/shared/models';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';

import { ContentType } from '@app/shared/enums';
import { Injectable } from '@angular/core';
import { map, Observable, catchError } from 'rxjs';
import { CUSTOM_HANDLE_ERROR_CONTEXT } from '@app/core/auth/auth.interceptor';

@Injectable()
export class AlgorithmEvaluationsService extends HttpService {
  protected url = 'flex-server/api/admin/algorithm';

  constructor(httpClient: HttpClient, private toastr: AppToastrService) {
    super(httpClient);
  }

  loadCollection(parameters: DefaultParameters): Observable<Pageable<AlgorithmEvaluationDTO>> {
    const { filters, ...params } = parameters;

    return this.getCollection(`${this.url}/evaluations/view`, {
      params: {
        ...params,
        ...filters,
      },
    });
  }

  generateResults(id: number) {
    return this.get<FileDTO>(`${this.url}/evaluation/${id}/results`).pipe(
      catchError((response: HttpErrorResponse): any => {
        if (!(response.status === 400 && response.error?.errorKey)) {
          this.toastr.error('algorithmEvaluations.actions.generateResults.error');
          return;
        }
      })
    ).subscribe(({ fileName, base64StringData }: FileDTO | any) => {
        DownloadService.saveFileWithParam(base64StringData, fileName, ContentType.ZIP, true);
        this.toastr.success('algorithmEvaluations.actions.generateResults.success');
      });
  }

  getKDMModelsDictionary(): Observable<Array<Dictionary>> {
    return this.get<{ id: number; value: string }[]>('flex-agno/api/admin/kdm-models/get-all-min').pipe(
      map(response => response.map(({ id, value }) => ({ id, value: id, label: value })))
    );
  }

  getLogs(id: number): Observable<AlgorithmEvaluationLogDTO[]> {
    return this.get(`${this.url}/offers/algorithm/get-logs/${id}`);
  }

  downloadInput(id: number): void {
    this.get<FileDTO>(`${this.url}/offers/algorithm/download/input/${id}`).subscribe(({ fileName, base64StringData }: FileDTO) =>
      DownloadService.saveFileWithParam(base64StringData, fileName, ContentType.ZIP, true)
    );
  }

  downloadOutput(id: number): void {
    this.get<FileDTO>(`${this.url}/offers/algorithm/download/output/${id}`).subscribe(({ fileName, base64StringData }: FileDTO) =>
      DownloadService.saveFileWithParam(base64StringData, fileName, ContentType.ZIP, true)
    );
  }

  exportEvaluation(id: number): Observable<FileDTO> {
    return this.get<FileDTO>(`${this.url}/${id}/offers/export`);
  }

  parsePBCMResult(id: number): Observable<void> {
    return this.get(`${this.url}/evaluations/pbcm/parse-results/${id}`, { context: CUSTOM_HANDLE_ERROR_CONTEXT });
  }

  parseDANOResult(id: number): Observable<void> {
    return this.get(`${this.url}/evaluations/dano/parse-results/${id}`, { context: CUSTOM_HANDLE_ERROR_CONTEXT });
  }

  cancelEvaluation(id: number): Observable<void> {
    return this.post(`${this.url}/evaluations/cancel-evaluation/${id}`, null);
  }
}
