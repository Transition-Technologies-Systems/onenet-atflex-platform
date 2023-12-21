import { Injectable } from '@angular/core';
import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';

@Injectable({
  providedIn: 'root',
})
export class KpiFiltersService {
  constructor(private fb: UntypedFormBuilder) {}

  createFormFilter(): UntypedFormGroup {
    return this.fb.group({
      type: this.fb.control(null, Validators.required),
      date: this.fb.group({
        from: [],
        to: [],
      }),
    });
  }
}
