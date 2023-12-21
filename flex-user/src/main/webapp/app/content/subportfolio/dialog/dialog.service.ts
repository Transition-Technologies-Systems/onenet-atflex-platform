import * as moment from 'moment';

import { AuthService, HttpService } from '@app/core';
import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';

import { Dictionary } from '@app/shared/models';
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { RequiredNoWhitespaceValidator } from '@app/shared/commons/validators';
import { Role } from '@app/shared/enums';
import { SubportfolioDTO } from '../subportfolio';
import { UnitDTO } from '@app/content/units/unit';
import { map } from 'rxjs/operators';
import { noBeforeCurrentHour } from '@app/shared/validators';

@Injectable()
export class SubportfoliosDialogService extends HttpService {
  constructor(httpClient: HttpClient, private fb: UntypedFormBuilder, private authService: AuthService) {
    super(httpClient);
  }

  createForm(data: Partial<SubportfolioDTO> = {}, hasRole: boolean = false): UntypedFormGroup {
    const disabledValidFrom = data.validFrom ? moment(data.validFrom).isBefore(moment()) : false;
    const disabledActive = data.validTo ? moment(data.validTo).isSameOrBefore(moment()) : false;
    const unitIds = (data.units || []).map((unit: UnitDTO) => unit.id);
    const fspId = data.fspId;

    const form = this.fb.group({
      id: [{ value: data.id, disabled: true }],
      name: [data.name, [RequiredNoWhitespaceValidator, Validators.maxLength(50)]],
      numberOfDers: [data.numberOfDers],
      combinedPowerOfDers: [data.combinedPowerOfDers],
      couplingPointIdTypes: [data.couplingPointIdTypes],
      mrid: [data.mrid, Validators.maxLength(50)],
      unitIds: [unitIds],
      certified: [{ value: !!data.certified, disabled: !hasRole }],
      active: [{ value: !!data.active, disabled: disabledActive }],
      fspId: [{ value: fspId, disabled: true }, Validators.required],
      validFrom: [
        {
          value: data.validFrom ? moment(data.validFrom).toDate() : null,
          disabled: disabledValidFrom,
        },
        disabledValidFrom ? [Validators.required] : [noBeforeCurrentHour, Validators.required],
      ],
      validTo: [data.validTo ? moment(data.validTo).toDate() : null, Validators.required],
    });

    this.authService.hasAnyRoles([Role.ROLE_ADMIN, Role.ROLE_DISTRIBUTION_SYSTEM_OPERATOR]).then((hasRole: boolean) => {
      if (!hasRole) {
        form.get('couplingPointIdTypes')?.disable();
        form.get('mrid')?.disable();
      }
    });

    if (data.certified) {
      Object.keys(form.controls).forEach((controlKey: string) => {
        form.get(controlKey)?.disable();
      });
    }

    return form;
  }

  getCompanies(roles?: Role[]): Observable<Dictionary[]> {
    return this.get<{ id: number; companyName: string }[]>('api/fsps/get-company', {
      params: {
        roles: roles ? roles : [Role.ROLE_FLEX_SERVICE_PROVIDER, Role.ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED],
      },
    }).pipe(map(response => response.map(({ id, companyName }) => ({ id, value: id, label: companyName }))));
  }

  getUnits(subportfolioId?: number): Observable<Dictionary[]> {
    return this.get<{ id: number; name: string; sder: boolean }[]>('api/user/units/get-all-for-subportfolio-modal-select', {
      params: {
        subportfolioId,
      },
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
}
