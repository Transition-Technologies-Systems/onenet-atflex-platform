import { HttpService } from '@app/core';
import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';

import { AppService } from '@app/app.service';
import { Dictionary } from '@app/shared/models';
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { RequiredNoWhitespaceValidator } from '@app/shared/commons/validators';
import { Role } from '@app/shared/enums';
import { SchedulingUnitDTO } from '../scheduling-units';
import { map } from 'rxjs/operators';

@Injectable()
export class SchedulingUnitsDialogService extends HttpService {
  constructor(httpClient: HttpClient, private fb: UntypedFormBuilder) {
    super(httpClient);
  }

  createForm(data: Partial<SchedulingUnitDTO> = {}): UntypedFormGroup {
    const numberOfDers = data.numberOfDers || 0;

    return this.fb.group({
      id: [{ value: data.id, disabled: true }],
      name: [data.name, [RequiredNoWhitespaceValidator, Validators.maxLength(50)]],
      schedulingUnitType: [data.schedulingUnitType, Validators.required],
      bspId: [{ value: data.bsp?.id || AppService.bspId, disabled: true }, Validators.required],
      bspName: [{ value: data.bsp?.representative?.companyName, disabled: true }],
      active: [!!data.active],
      readyForTests: [{ value: !!data.readyForTests, disabled: numberOfDers < 1 }],
      acceptedDers: [{ value: [], disabled: data.certified || data.readyForTests }],
      couplingPoints: [{ value: data.couplingPoints, disabled: true }],
      primaryCouplingPoint: [{ value: data.primaryCouplingPoint, disabled: !data.couplingPoints?.length }],
      certified: [{ value: !!data.certified, disabled: true }],
    });
  }

  getCompanies(): Observable<Dictionary[]> {
    return this.get<{ id: number; companyName: string }[]>('api/fsps/get-company', {
      params: {
        roles: [Role.ROLE_BALANCING_SERVICE_PROVIDER],
        'certified.equals': true,
      },
    }).pipe(map(response => response.map(({ id, companyName }) => ({ id, value: id, label: companyName }))));
  }
}
