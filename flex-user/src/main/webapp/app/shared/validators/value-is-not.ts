import { AbstractControl, ValidationErrors } from '@angular/forms';

export const valusIsNotZero = (control: AbstractControl): ValidationErrors | null => {
  if (control.value === 0) {
    return {
      disabledValue: true,
    };
  }

  return null;
};
