import { UntypedFormBuilder, UntypedFormGroup } from '@angular/forms';

import { Injectable } from '@angular/core';

@Injectable()
export class UnitsFiltersService {
  constructor(private fb: UntypedFormBuilder) {}

  createFormFilter(): UntypedFormGroup {
    return this.fb.group({
      derTypeId: [],
      aggregated: [],
      createdDate: this.fb.group({
        from: [],
        to: [],
      }),
      lastModifiedDate: this.fb.group({
        from: [],
        to: [],
      }),
      active: [],
      certified: [],

      unitId: [],
    });
  }
}
