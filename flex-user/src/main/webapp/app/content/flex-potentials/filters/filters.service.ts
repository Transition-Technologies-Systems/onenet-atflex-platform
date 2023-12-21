import { Injectable } from '@angular/core';
import { UntypedFormBuilder, UntypedFormGroup } from '@angular/forms';

@Injectable()
export class FlexPotentialsFiltersService {
  constructor(private fb: UntypedFormBuilder) {}

  createFormFilter(): UntypedFormGroup {
    return this.fb.group({
      unitName: [],
      valid: this.fb.group({
        from: [],
        to: [],
      }),
    });
  }
}
