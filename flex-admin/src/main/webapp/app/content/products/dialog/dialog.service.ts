import * as moment from 'moment';

import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { MaxOrSameControlValidator, RequiredNoWhitespaceValidator } from '@app/shared/commons/validators';

import { HttpClient } from '@angular/common/http';
import { HttpService } from '@app/core';
import { Injectable } from '@angular/core';
import { ProductDTO } from '@app/shared/models';
import { RequireOneValidator } from '@app/shared/commons/validators/require-one.validator';
import { UpdateValueAndValidity } from '@app/shared/commons/validators/update-validity';
import { noBeforeCurrentHour } from '@app/shared/validators';

@Injectable()
export class ProductsDialogService extends HttpService {
  constructor(httpClient: HttpClient, private fb: UntypedFormBuilder) {
    super(httpClient);
  }

  createForm(data: Partial<ProductDTO> = {}, mode: 'add' | 'edit'): UntypedFormGroup {
    const disabledValidFrom = data.validFrom ? moment(data.validFrom).isBefore(moment()) : false;
    const disabledActive = data.validTo ? moment(data.validTo).isSameOrBefore(moment()) : false;

    return this.fb.group({
      id: [{ value: data.id, disabled: true }],
      fullName: [data.fullName, [RequiredNoWhitespaceValidator, Validators.maxLength(255)]],
      shortName: [data.shortName, [RequiredNoWhitespaceValidator, Validators.maxLength(50)]],
      locational: [!!data.locational],
      minBidSize: [data.minBidSize, [Validators.required, MaxOrSameControlValidator('maxBidSize')]],
      maxBidSize: [data.maxBidSize, [Validators.required, UpdateValueAndValidity('minBidSize')]],
      bidSizeUnit: [data.bidSizeUnit, Validators.required],
      direction: [data.direction, Validators.required],
      maxFullActivationTime: [data.maxFullActivationTime, Validators.required],
      minRequiredDeliveryDuration: [data.minRequiredDeliveryDuration, Validators.required],
      active: [{ value: !!data.active, disabled: disabledActive }],
      validFrom: [
        {
          value: data.validFrom ? moment(data.validFrom).toDate() : null,
          disabled: disabledValidFrom,
        },
        disabledValidFrom ? [Validators.required] : [noBeforeCurrentHour, Validators.required],
      ],
      validTo: [data.validTo ? moment(data.validTo).toDate() : null, Validators.required],
      psoUserId: [data.psoUserId, Validators.required],
      ssoUserIds: [data.ssoUserIds, Validators.required],
      balancing: [!!data.balancing, [RequireOneValidator(['balancing', 'cmvc'])]],
      cmvc: [!!data.cmvc, [RequireOneValidator(['balancing', 'cmvc'])]],
      version: [data.version],
    });
  }
}
