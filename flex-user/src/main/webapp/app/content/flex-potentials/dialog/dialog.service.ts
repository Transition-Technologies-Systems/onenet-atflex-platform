import * as moment from 'moment';

import { AuthService, HttpService } from '@app/core';
import { Dictionary, FlexPotentialDTO, ProductDTO } from '@app/shared/models';
import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { minArrayLength, noBeforeCurrentHour } from '@app/shared/validators';

import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Role } from '@app/shared/enums';
import { map } from 'rxjs/operators';

@Injectable()
export class FlexPotentialsDialogService extends HttpService {
  constructor(httpClient: HttpClient, private fb: UntypedFormBuilder, private authService: AuthService) {
    super(httpClient);
  }

  createForm(data: Partial<FlexPotentialDTO> = {}, mode: 'add' | 'edit'): UntypedFormGroup {
    const disabledValidFrom = data.validFrom ? moment(data.validFrom).isBefore(moment()) : false;
    const disabledActive = data.validTo ? moment(data.validTo).isSameOrBefore(moment()) : false;
    const units = data.units || [];

    const form = this.fb.group({
      id: [{ value: data.id, disabled: true }],
      productId: [{ value: data.product?.id, disabled: mode === 'edit' }, Validators.required],
      unitIds: [units.map(({ id }) => id), minArrayLength(1)],
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

    this.authService.hasRole(Role.ROLE_FLEX_SERVICE_PROVIDER).then((isFsp: boolean) => {
      if (isFsp) {
        form.get('productPrequalification')?.disable();
        form.get('staticGridPrequalification')?.disable();
        form.get('unitId')?.setValidators(Validators.required);
      }
    });

    this.authService.hasRole(Role.ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED).then((isFspAggregated: boolean) => {
      if (isFspAggregated) {
        form.get('productPrequalification')?.disable();
        form.get('staticGridPrequalification')?.disable();
        form.get('unitIds')?.setValidators(Validators.required);
      }
    });

    this.authService.hasRole(Role.ROLE_BALANCING_SERVICE_PROVIDER).then((isBsp: boolean) => {
      if (isBsp) {
        form.get('productId')?.disable();
      }
    });

    if (data.productPrequalification && data.staticGridPrequalification) {
      Object.keys(form.controls).forEach((controlKey: string) => {
        form.get(controlKey)?.disable();
      });
    }

    return form;
  }

  getCompanies(): Observable<Dictionary[]> {
    return this.get<{ id: number; companyName: string }[]>('api/fsps/get-company', {
      params: {
        roles: [Role.ROLE_FLEX_SERVICE_PROVIDER, Role.ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED],
      },
    }).pipe(map(response => response.map(({ id, companyName }) => ({ id, value: id, label: companyName }))));
  }

  getProduct(id: number): Observable<ProductDTO> {
    return this.get(`api/user/products/${id}`);
  }

  getScheduleUnits(): Observable<Dictionary[]> {
    return this.get<{ id: number; name: string; product: ProductDTO }[]>('api/user/scheduling-units/minimal').pipe(
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
