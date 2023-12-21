import * as moment from 'moment';

import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';

import { FspDTO } from '@app/shared/models';
import { HttpClient } from '@angular/common/http';
import { HttpService } from '@app/core';
import { Injectable } from '@angular/core';
import { RequiredNoWhitespaceValidator } from '@app/shared/commons/validators';
import { noBeforeCurrentHour } from '@app/shared/validators';

@Injectable()
export class FspsDialogService extends HttpService {
  constructor(httpClient: HttpClient, private fb: UntypedFormBuilder) {
    super(httpClient);
  }

  createForm(data: Partial<FspDTO> = {}): UntypedFormGroup {
    const disabledValidFrom = data.validFrom ? moment(data.validFrom).isBefore(moment()) : false;
    const disabledActive = data.validTo ? moment(data.validTo).isSameOrBefore(moment()) : false;

    return this.fb.group({
      id: [{ value: data.id, disabled: true }],
      active: [{ value: !!data.active, disabled: disabledActive }],
      agreementWithTso: [!!data.agreementWithTso],
      validFrom: [
        {
          value: data.validFrom ? moment(data.validFrom).toDate() : null,
          disabled: disabledValidFrom,
        },
        disabledValidFrom ? [Validators.required] : [noBeforeCurrentHour, Validators.required],
      ],
      validTo: [data.validTo ? moment(data.validTo).toDate() : null, Validators.required],
      companyName: [data.companyName, [RequiredNoWhitespaceValidator, Validators.maxLength(254)]],
      representative: this.fb.group({
        id: [{ value: data.representative?.id, disabled: true }],
        firstName: [data.representative?.firstName, [RequiredNoWhitespaceValidator, Validators.maxLength(50)]],
        lastName: [data.representative?.lastName, [RequiredNoWhitespaceValidator, Validators.maxLength(50)]],
        email: [data.representative?.email, [Validators.required, Validators.email]],
        phoneNumber: [data.representative?.phoneNumber, Validators.required],
      }),
    });
  }
}
