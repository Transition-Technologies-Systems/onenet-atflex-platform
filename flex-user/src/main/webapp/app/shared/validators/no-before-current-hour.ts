import * as moment from 'moment';

import { AbstractControl, ValidationErrors } from '@angular/forms';

export const noBeforeCurrentHour = (control: AbstractControl): ValidationErrors | null => {
  if (!control.value) {
    return null;
  }

  const date = moment(control.value);

  if (date.isSameOrAfter(moment().set({ m: 0, s: 0, ms: 0 }))) {
    return null;
  }

  return { beforeCurrentHour: true };
};
