import { Injectable } from '@angular/core';
import { UntypedFormBuilder, UntypedFormGroup } from '@angular/forms';

@Injectable()
export class ProductsFiltersService {
  constructor(private fb: UntypedFormBuilder) {}

  createFormFilter(): UntypedFormGroup {
    return this.fb.group({
      active: [],
      locational: [],
    });
  }
}
