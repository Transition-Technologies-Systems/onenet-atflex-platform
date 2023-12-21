import { Dictionary, Pageable } from '@app/shared/models';
import { Observable, map } from 'rxjs';
import { PartnershipDTO, Tab, TabType } from './partnership';
import { ProposalDTO, ProposalStatus } from '@app/shared/proposal';

import { HttpClient } from '@angular/common/http';
import { HttpService } from '@app/core';
import { Injectable } from '@angular/core';
import { PartnershipParameters } from './partnership.store';
import { TranslateService } from '@ngx-translate/core';

@Injectable()
export class PartnershipService extends HttpService {
  protected url = 'flex-server/api/admin/scheduling-units/proposal/get-all';

  constructor(httpClient: HttpClient, private translate: TranslateService) {
    super(httpClient);
  }

  cancel(proposalId: number): Observable<any> {
    return this.get('flex-server/api/admin/scheduling-units/proposal/cancel', {
      params: { proposalId },
    });
  }

  getBspSenders(proposalType: TabType): Observable<Dictionary[]> {
    return this.get<Dictionary[]>('flex-server/api/admin/scheduling-units/proposal/bsp/get-fsps-used-in-bsp-proposals', {
      params: { proposalType },
    }).pipe(map(response => response.map(({ id, companyName }) => ({ id, value: id, label: companyName }))));
  }

  getFspSenders(proposalType: TabType): Observable<Dictionary[]> {
    return this.get<Dictionary[]>('flex-server/api/admin/scheduling-units/proposal/fsp/get-bsps-used-in-fsp-proposals', {
      params: { proposalType },
    }).pipe(map(response => response.map(({ id, companyName }) => ({ id, value: id, label: companyName }))));
  }

  getTabs(): Tab[] {
    return [
      {
        label: this.translate.instant('partnership.tabs.invitation'),
        type: 'INVITATION',
      },
      {
        label: this.translate.instant('partnership.tabs.request'),
        type: 'REQUEST',
      },
    ];
  }

  loadCollection(parameters: PartnershipParameters): Observable<Pageable<PartnershipDTO>> {
    const { tabType, filters, ...params } = parameters;

    return this.getCollection<PartnershipDTO>(`${this.url}`, {
      params: {
        ...params,
        ...filters,
        'proposalType.in': tabType,
      },
    });
  }

  resend(row: PartnershipDTO, type: TabType): Observable<any> {
    const url = type === 'INVITATION' ? 'invite-der' : 'propose-der';

    const data: Partial<ProposalDTO> = {
      id: row.id,
      status: ProposalStatus.NEW,
      schedulingUnitId: row.schedulingUnitId,
      unitId: row.derId,
      senderId: type === 'INVITATION' ? row.bspId : row.fspId,
    };

    return this.post(`flex-server/api/admin/scheduling-units/proposal/${url}`, data);
  }
}
