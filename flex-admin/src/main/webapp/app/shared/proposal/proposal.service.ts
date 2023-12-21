import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { Observable, map, of } from 'rxjs';

import { Dictionary } from '@app/shared/models';
import { HttpClient } from '@angular/common/http';
import { HttpService } from '@app/core';
import { Injectable } from '@angular/core';
import { ProposalDTO, ProposalSubportfolioType } from './proposal';
import { Role } from '../enums';
import { TranslateService } from '@ngx-translate/core';

@Injectable()
export class ProposalService extends HttpService {
  protected url = 'flex-server/api/admin/scheduling-units/proposal';

  constructor(httpClient: HttpClient, private fb: UntypedFormBuilder, private translate: TranslateService) {
    super(httpClient);
  }

  createConfirmForm(): UntypedFormGroup {
    return this.fb.group({
      schedulingUnitId: [null, Validators.required],
    });
  }

  createForm(): UntypedFormGroup {
    return this.fb.group({
      fsp: [null, Validators.required],
      unitId: [null, Validators.required],
      subportfolio: [null, Validators.required],
    });
  }

  getCompanies(bspId: number | undefined): Observable<Dictionary[]> {
    if (!bspId) {
      return of([]);
    }

    return this.get<{ id: number; companyName: string; role: Role }[]>('flex-server/api/fsps/get-company', {
      params: {
        roles: [Role.ROLE_FLEX_SERVICE_PROVIDER, Role.ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED],
      },
    }).pipe(
      map(response =>
        response.map(({ id, companyName, role }) => {
          const roleName = this.translate.instant(`RoleShort.${role}`);

          return { id, value: id, label: `${companyName} (${roleName})`, role };
        })
      )
    );
  }

  getDers(subportfolioId: number | ProposalSubportfolioType, bspId: number, fspaId: number): Observable<Dictionary[]> {
    if (!subportfolioId || !bspId) {
      return of([]);
    }

    const params = subportfolioId === ProposalSubportfolioType.NOSUBPORTFOLIO ? { bspId, fspaId } : { subportfolioId, bspId };

    return this.get<{ id: number; name: string; sder: boolean }[]>(
      'flex-server/api/admin/scheduling-units/proposal/fspa/get-available-ders',
      {
        params,
      }
    ).pipe(
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
      'flex-server/api/admin/scheduling-units/minimal/get-all-bsp-su-to-which-ones-fsp-der-can-be-joined',
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

  getSubportfolios(fspaId: number): Observable<Dictionary[]> {
    return this.get<Dictionary[]>('flex-server/api/admin/subportfolio/minimal/get-fspa-certified-subs', {
      params: { fspaId },
    }).pipe(
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

  getUnits(bspId: number, fspId: number): Observable<Dictionary[]> {
    return this.get<{ id: number; name: string; sder: boolean }[]>(
      'flex-server/api/admin/scheduling-units/proposal/fsp/get-available-ders',
      {
        params: {
          bspId,
          fspId,
        },
      }
    ).pipe(
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
