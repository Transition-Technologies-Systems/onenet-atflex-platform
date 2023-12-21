import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { Observable, map, of } from 'rxjs';

import { Dictionary } from '@app/shared/models';
import { HttpClient } from '@angular/common/http';
import { HttpService } from '@app/core';
import { Injectable } from '@angular/core';
import { ProposalDTO, ProposalSubportfolioType } from './proposal';

@Injectable()
export class ProposalService extends HttpService {
  protected url = 'api/user/scheduling-units/proposal';

  constructor(httpClient: HttpClient, private fb: UntypedFormBuilder) {
    super(httpClient);
  }

  createConfirmForm(): UntypedFormGroup {
    return this.fb.group({
      schedulingUnitId: [null, Validators.required],
    });
  }

  createForm(): UntypedFormGroup {
    return this.fb.group({
      subportfolio: [],
      unitId: [null, Validators.required],
    });
  }

  getDers(subportfolioId: number | ProposalSubportfolioType, bspId?: number): Observable<Dictionary[]> {
    if (!subportfolioId || !bspId) {
      return of([]);
    }

    const params = subportfolioId === ProposalSubportfolioType.NOSUBPORTFOLIO ? { bspId } : { subportfolioId, bspId };

    return this.get<{ id: number; name: string; sder: boolean }[]>('api/user/scheduling-units/proposal/fspa/get-available-ders', {
      params,
    }).pipe(
      map(response =>
        (response || []).map(({ id, name, sder }) => ({
          id,
          value: id,
          label: `${name}${sder ? '(SDER)' : ''}`,
        }))
      )
    );
  }

  getProposal(proposalId: number): Observable<ProposalDTO> {
    return this.get(`${this.url}/get`, {
      params: {
        proposalId,
      },
    });
  }

  getScheduleUnits(derId: number): Observable<Dictionary[]> {
    return this.get<{ id: number; name: string }[]>(
      'api/user/scheduling-units/minimal/get-all-bsp-su-to-which-ones-fsp-der-can-be-joined',
      {
        params: { derId },
      }
    ).pipe(
      map(response =>
        response.map(({ id, name }) => ({
          id,
          value: id,
          label: name,
        }))
      )
    );
  }

  getSubportfolios(): Observable<Dictionary[]> {
    return this.get<Dictionary[]>('api/user/subportfolio/minimal/get-fspa-certified-subs').pipe(
      map(response => [
        {
          id: ProposalSubportfolioType.NOSUBPORTFOLIO,
          value: ProposalSubportfolioType.NOSUBPORTFOLIO,
          name: 'shared.proposal.no-subportfolio',
          italic: true,
          first: true,
        },
        ...(response ?? []),
      ])
    );
  }

  getUnits(bspId?: number): Observable<Dictionary[]> {
    return this.get<{ id: number; name: string; sder: boolean }[]>('api/user/scheduling-units/proposal/fsp/get-available-ders', {
      params: { bspId },
    }).pipe(
      map(response =>
        (response || []).map(({ id, name, sder }) => ({
          id,
          value: id,
          label: `${name}${sder ? '(SDER)' : ''}`,
        }))
      )
    );
  }

  proposalAccept(proposalId: number, type: 'FSP' | 'BSP', schedulingUnitId?: number): Observable<void> {
    const prefix = type === 'FSP' ? 'bsp' : 'fsp';

    return this.get(`${this.url}/${prefix}/accept`, {
      params: {
        proposalId,
        schedulingUnitId,
      },
    });
  }

  proposalReject(proposalId: number): Observable<void> {
    return this.get(`${this.url}/reject`, {
      params: {
        proposalId,
      },
    });
  }

  saveProposal(data: Partial<ProposalDTO>): Observable<void> {
    return this.post(`${this.url}/propose-der`, data);
  }
}
