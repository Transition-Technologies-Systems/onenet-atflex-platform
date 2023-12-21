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
    const isEdit = !!data.id;
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
      fspId: [{ value: fspId, disabled: isEdit }, Validators.required],
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

    this.authService.hasRole(Role.ROLE_TRANSMISSION_SYSTEM_OPERATOR).then((hasRole: boolean) => {
      if (!hasRole) {
        return;
      }

      Object.keys(form.controls).forEach((controlKey: string) => {
        if (['certified'].includes(controlKey)) {
          return;
        }

        form.get(controlKey)?.disable();
      });
    });

    this.authService.hasRole(Role.ROLE_DISTRIBUTION_SYSTEM_OPERATOR).then((hasRole: boolean) => {
      if (!hasRole) {
        return;
      }

      Object.keys(form.controls).forEach((controlKey: string) => {
        if (['certified', 'couplingPointIdTypes', 'mrid'].includes(controlKey)) {
          return;
        }

        form.get(controlKey)?.disable();
      });
    });

    return form;
  }

  getCompanies(selectedIds: number[], data: Partial<SubportfolioDTO>): Observable<Dictionary[]> {
    const currentFspa = data.fspa;

    return this.get<{ id: number; companyName: string }[]>('flex-server/api/fsps/get-company', {
      params: {
        roles: [Role.ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED],
        selectedIds,
      },
    }).pipe(
      map(response => {
        const existCurrentId = response.find(({ id }) => id === currentFspa?.id);

        if (currentFspa && !existCurrentId) {
          response.push({
            id: currentFspa.id,
            companyName: currentFspa.companyName,
          });
        }

        return response.map(({ id, companyName }) => ({ id, value: id, label: companyName }));
      })
    );
  }

  getUnits(fspaId: number, subportfolioId?: number): Observable<Dictionary[]> {
    return this.get<{ id: number; name: string; sder: boolean }[]>(`flex-server/api/admin/units/get-all-for-subportfolio-modal-select`, {
      params: {
        fspaId,
        subportfolioId,
      },
    }).pipe(
      map(response =>
        response.map(({ id, name, sder, ...rest }) => ({
          id,
          name,
          sder,
          ...rest,
          value: id,
          label: `${name}${sder ? '(SDER)' : ''}`,
        }))
      )
    );
  }
}
