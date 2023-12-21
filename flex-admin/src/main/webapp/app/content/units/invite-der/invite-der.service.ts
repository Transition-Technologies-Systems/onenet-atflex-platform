import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { Observable, map } from 'rxjs';

import { Dictionary } from '@app/shared/models';
import { HttpClient } from '@angular/common/http';
import { HttpService } from '@app/core';
import { Injectable } from '@angular/core';
import { ProposalDTO } from '@app/shared/proposal';
import { Role } from '@app/shared/enums';

@Injectable()
export class UnitsInviteDerService extends HttpService {
  protected url = 'flex-server/api/admin/scheduling-units/proposal/invite-der';

  constructor(httpClient: HttpClient, private fb: UntypedFormBuilder) {
    super(httpClient);
  }

  createForm(): UntypedFormGroup {
    return this.fb.group({
      schedulingUnitId: [null, Validators.required],
      bspId: [null, Validators.required],
    });
  }

  getProposal(key: string): Observable<ProposalDTO> {
    return this.get(`${this.url}/get-by-key`, {
      params: {
        key,
      },
    });
  }

  proposalAccept(key: string): Observable<void> {
    return this.get(`${this.url}/accept-by-key`, {
      params: {
        key,
      },
    });
  }

  proposalReject(key: string): Observable<void> {
    return this.get(`${this.url}/reject-by-key`, {
      params: {
        key,
      },
    });
  }

  getCompanies(): Observable<Dictionary[]> {
    return this.get<{ id: number; companyName: string }[]>('flex-server/api/fsps/get-company', {
      params: {
        roles: [Role.ROLE_BALANCING_SERVICE_PROVIDER],
      },
    }).pipe(map(response => response.map(({ id, companyName }) => ({ id, value: id, label: companyName }))));
  }

  getScheduleUnits(bspId: number, derId: number): Observable<Dictionary[]> {
    return this.get<{ id: number; name: string }[]>(
      'flex-server/api/admin/scheduling-units/minimal/get-all-bsp-su-to-which-ones-fsp-der-can-be-joined',
      {
        params: {
          bspId,
          derId,
        },
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
}
