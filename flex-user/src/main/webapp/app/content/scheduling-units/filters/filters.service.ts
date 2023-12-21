import { UntypedFormBuilder, UntypedFormGroup } from '@angular/forms';

import { Injectable } from '@angular/core';

@Injectable()
export class SchedulingUnitsFiltersService {
  constructor(private fb: UntypedFormBuilder) {}

  createFormFilter(): UntypedFormGroup {
    return this.fb.group({
      schedulingUnitTypeId: [],
      createdDate: this.fb.group({
        from: [],
        to: [],
      }),
      lastModifiedDate: this.fb.group({
        from: [],
        to: [],
      }),
      active: [],
    });
  }
}
