import { UntypedFormBuilder, UntypedFormGroup } from '@angular/forms';

import { HttpClient } from '@angular/common/http';
import { HttpService } from '@app/core';
import { Injectable } from '@angular/core';

@Injectable()
export class PartnershipFiltersService extends HttpService {
  constructor(httpClient: HttpClient, private fb: UntypedFormBuilder) {
    super(httpClient);
  }

  createFormFilter(): UntypedFormGroup {
    return this.fb.group({
      status: [],
      sender: [],
      receiver: [],
    });
  }
}
