import { UntypedFormBuilder, UntypedFormGroup } from '@angular/forms';

import { Injectable } from '@angular/core';

@Injectable()
export class AuctionsDayAheadFiltersService {
  constructor(private fb: UntypedFormBuilder) {}

  createFormFilter(): UntypedFormGroup {
    return this.fb.group({
      productId: [],
      deliveryDate: [],
      gateDate: this.fb.group({
        from: [],
        to: [],
      }),
      createdDate: this.fb.group({
        from: [],
        to: [],
      }),
      lastModifiedDate: this.fb.group({
        from: [],
        to: [],
      }),
    });
  }
}
