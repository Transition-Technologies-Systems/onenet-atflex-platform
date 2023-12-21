import { AlgorithmEvaluationConfigDTO, DefaultParameters, Dictionary, FileDTO, Pageable } from '@app/shared/models';
import { AppToastrService, DownloadService, HttpService } from '@app/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, Subscription, map } from 'rxjs';
import { AlgorithmType } from '@app/shared/enums';

import { AuctionOfferStatus, AuctionType } from '../enums';
import { BidsEvaluationDTO } from './bids-evaluation';
import { ContentType } from '@app/shared/enums';
import { Injectable } from '@angular/core';
import { CUSTOM_HANDLE_ERROR_CONTEXT } from '@app/core/auth/auth.interceptor';

@Injectable()
export class BidsEvaluationService extends HttpService {
  protected url = 'flex-server/api/admin/auctions/offers/view';

  constructor(httpClient: HttpClient, private toastr: AppToastrService) {
    super(httpClient);
  }

  exportXLSX(filters: any, isSETO: boolean): Subscription {
    return this.get<FileDTO>(`${this.url}/export${isSETO ? '/seto' : ''}`, {
      params: filters,
    }).subscribe(({ fileName, base64StringData }: FileDTO) =>
      DownloadService.saveFileWithParam(base64StringData, fileName, ContentType.XLSX, true)
    );
  }

  getBmAgnoFiles(): void {
    this.get<FileDTO>('flex-server/api/admin/auctions-day-ahead/offers/get-bm-agno-files').subscribe(
      ({ fileName, base64StringData }: FileDTO) => DownloadService.saveFileWithParam(base64StringData, fileName, ContentType.ZIP, true)
    );
  }

  getPbcmAgnoFiles(): void {
    this.get<FileDTO>('flex-server/api/admin/auctions-day-ahead/offers/get-pbcm-agno-files').subscribe(
      ({ fileName, base64StringData }: FileDTO) => DownloadService.saveFileWithParam(base64StringData, fileName, ContentType.ZIP, true)
    );
  }

  getProducts(): Observable<Array<Dictionary>> {
    return this.get<{ id: number; name: string }[]>('flex-server/api/admin/auctions/offers/get-products-used-in-offers').pipe(
      map(response => response.map(({ id, name }) => ({ id, value: id, label: name })))
    );
  }

  getKDMModels(): Observable<Array<Dictionary>> {
    return this.get<{ id: number; areaName: string; lvModel: boolean }[]>('flex-agno/api/admin/kdm-models/get-all').pipe(
      map(response => response.map(({ id, areaName, lvModel }) => ({ id, value: id, label: areaName, lvModel })))
    );
  }

  import(data: FormData, typeOfImport: string): Observable<{ importedBids: number[]; notImportedBids: any[] }> {
    let url = '';
    const { DANO, AGNO, CMVC } = AuctionType;
    switch (typeOfImport) {
      case DANO:
        url = `${this.url}/import/pbcm-dano`;
        break;
      case AGNO:
        url = 'flex-server/api/admin/auctions-day-ahead/offer-update-import';
        break;
      case CMVC:
        url = `${this.url}/import/cmvc`;
        break;
      case 'importDSO':
        url = 'flex-server/api/admin/auctions/offers/view/import/seto';
        break;
    }
    return this.post(url, data, { context: CUSTOM_HANDLE_ERROR_CONTEXT });
  }

  mark(status: AuctionOfferStatus, data: BidsEvaluationDTO[]): Observable<void> {
    const ids = data.map(({ id }) => id);

    return this.request('PATCH', `${this.url}/mark`, {
      params: {
        ids,
        status,
      },
    });
  }

  runAlgorithm(config: AlgorithmEvaluationConfigDTO, algorithmType: AlgorithmType): Observable<void> {
    let endpoint = '';
    switch (algorithmType) {
      case AlgorithmType.BM:
        endpoint = '/run-agno-algorithm';
        break;
      case AlgorithmType.PBCM:
        endpoint = '/run-pbcm-algorithm';
        break;
      case AlgorithmType.DANO:
        endpoint = '/run-dano-algorithm';
        break;
    }
    return this.post(`flex-server/api/admin/algorithm/evaluations${endpoint}`, config);
  }

  loadCollection(parameters: DefaultParameters): Observable<Pageable<BidsEvaluationDTO>> {
    const { filters, ...params } = parameters;

    return this.getCollection(this.url, {
      params: {
        ...params,
        ...filters,
      },
    });
  }
}
