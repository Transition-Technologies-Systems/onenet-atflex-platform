import { UntypedFormBuilder, UntypedFormGroup } from '@angular/forms';

import { Injectable } from '@angular/core';

@Injectable()
export class FspsFiltersService {
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
      valid: this.fb.group({
        from: [],
        to: [],
      }),
      active: [],
    });
  }
}
