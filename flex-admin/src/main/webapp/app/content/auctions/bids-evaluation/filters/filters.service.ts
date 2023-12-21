import * as moment from 'moment';

import { UntypedFormBuilder, UntypedFormGroup } from '@angular/forms';

import { Injectable } from '@angular/core';

@Injectable()
export class BidsEvaluationFiltersService {
  constructor(private fb: UntypedFormBuilder) {}

  createFormFilter(): UntypedFormGroup {
    return this.fb.group({
      productId: [],
      deliveryPeriod: [moment().startOf('day').add(1, 'd').toDate()],
    });
  }
}
