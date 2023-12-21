import { UntypedFormBuilder, UntypedFormGroup } from '@angular/forms';

import { Injectable } from '@angular/core';

@Injectable()
export class ProductsFiltersService {
  constructor(private fb: UntypedFormBuilder) {}

  createFormFilter(): UntypedFormGroup {
    return this.fb.group({
      active: [],
      locational: [],

      productId: [],
    });
  }
}
