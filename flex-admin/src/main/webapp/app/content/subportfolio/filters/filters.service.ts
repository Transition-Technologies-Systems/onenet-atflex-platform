import { Injectable } from '@angular/core';
import { UntypedFormBuilder, UntypedFormGroup } from '@angular/forms';

@Injectable()
export class SubportfoliosFiltersService {
  constructor(private fb: UntypedFormBuilder) {}

  createFormFilter(): UntypedFormGroup {
    return this.fb.group({
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
    });
  }
}
