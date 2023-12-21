import { Injectable } from '@angular/core';
import { UntypedFormBuilder, UntypedFormGroup } from '@angular/forms';
import { ActivationsSettlementsDialogDTO } from '@app/shared/models/activations-settlements';

@Injectable()
export class ActivationsSettlementsDialogService {
  constructor(private fb: UntypedFormBuilder) {}

  createForm(data: Partial<ActivationsSettlementsDialogDTO> = {}): UntypedFormGroup {
    return this.fb.group({
      id: [{ value: data.id, disabled: true }],
      activatedVolume: [data.activatedVolume, []],
      settlementAmount: [data.settlementAmount, []],
    });
  }
}
