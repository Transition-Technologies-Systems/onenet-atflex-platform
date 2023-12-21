import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { Observable, map } from 'rxjs';

import { Dictionary } from '@app/shared/models';
import { HttpClient } from '@angular/common/http';
import { HttpService } from '@app/core';
import { Injectable } from '@angular/core';
import { ProposalDTO } from '@app/shared/proposal';

@Injectable()
export class UnitsInviteDerService extends HttpService {
  protected url = 'api/user/scheduling-units/proposal';

  constructor(httpClient: HttpClient, private fb: UntypedFormBuilder) {
    super(httpClient);
  }

  createForm(): UntypedFormGroup {
    return this.fb.group({
      schedulingUnitId: [null, Validators.required],
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
    return this.get<{ id: number; companyName: string }[]>('api/fsps/minimal/get-bsps-with-not-empty-scheduling-units').pipe(
      map(response => response.map(({ id, companyName }) => ({ id, value: id, label: companyName })))
    );
  }
}
