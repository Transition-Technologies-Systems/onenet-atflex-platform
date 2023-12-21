import * as moment from 'moment';

import { AuthService, HttpService } from '@app/core';
import { Dictionary, FlexPotentialDTO, ProductDTO } from '@app/shared/models';
import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { minArrayLength, noBeforeCurrentHour } from '@app/shared/validators';

import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Role } from '@app/shared/enums';
import { TranslateService } from '@ngx-translate/core';
import { map } from 'rxjs/operators';

@Injectable()
export class FlexPotentialsDialogService extends HttpService {
  constructor(
    httpClient: HttpClient,
    private fb: UntypedFormBuilder,
    private authService: AuthService,
    private translate: TranslateService
  ) {
    super(httpClient);
  }

  createForm(data: Partial<FlexPotentialDTO> = {}, mode: 'add' | 'edit'): UntypedFormGroup {
    const disabledValidFrom = data.validFrom ? moment(data.validFrom).isBefore(moment()) : false;
    const disabledActive = data.validTo ? moment(data.validTo).isSameOrBefore(moment()) : false;
    const units = data.units || [];

    const form = this.fb.group({
      id: [{ value: data.id, disabled: true }],
      productId: [{ value: data.product?.id, disabled: mode === 'edit' }, Validators.required],
      fsp: [
        {
          value: {
            id: data.fsp?.id,
            value: data.fsp?.id,
            role: data.fsp?.role,
            label: data.fsp?.representative?.companyName,
          },
          disabled: mode === 'edit',
        },
        Validators.required,
      ],
      unitIds: [{ value: units.map(({ id }) => id), disabled: !!data.fsp?.id ? false : true }, minArrayLength(1)],
      volume: [data.volume, Validators.required],
      volumeUnit: [{ value: data.volumeUnit, disabled: true }, Validators.required],
      divisibility: [mode === 'add' ? true : !!data.divisibility, Validators.required],
      active: [{ value: !!data.active, disabled: disabledActive }],
      validFrom: [
        {
          value: data.validFrom ? moment(data.validFrom).toDate() : null,
          disabled: disabledValidFrom,
        },
        disabledValidFrom ? [Validators.required] : [noBeforeCurrentHour, Validators.required],
      ],
      validTo: [data.validTo ? moment(data.validTo).toDate() : null, Validators.required],
      fullActivationTime: [data.fullActivationTime, Validators.required],
      minDeliveryDuration: [data.minDeliveryDuration, Validators.required],
      productPrequalification: [!!data.productPrequalification],
      staticGridPrequalification: [!!data.staticGridPrequalification],
      version: [data.version],
    });

    this.authService
      .hasAnyRoles([Role.ROLE_DISTRIBUTION_SYSTEM_OPERATOR, Role.ROLE_TRANSMISSION_SYSTEM_OPERATOR])
      .then((hasRole: boolean) => {
        if (!hasRole) {
          return;
        }

        Object.keys(form.controls).forEach((controlKey: string) => {
          if (['productPrequalification', 'staticGridPrequalification'].includes(controlKey)) {
            return;
          }

          form.get(controlKey)?.disable();
        });
      });

    return form;
  }

  getCompanies(): Observable<Dictionary[]> {
    return this.get<{ id: number; companyName: string; role: Role }[]>('flex-server/api/fsps/get-company-attached-unit', {
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

  getProduct(id: number): Observable<ProductDTO> {
    return this.get(`flex-server/api/admin/products/${id}`);
  }

  getScheduleUnits(bspId: number): Observable<Dictionary[]> {
    return this.get<{ id: number; name: string; product: ProductDTO }[]>('flex-server/api/admin/scheduling-units/minimal', {
      params: {
        'bspId.equals': bspId,
      },
    }).pipe(
      map(response =>
        response.map(({ id, name, product }) => ({
          id,
          value: id,
          label: name,
          product,
        }))
      )
    );
  }
}
