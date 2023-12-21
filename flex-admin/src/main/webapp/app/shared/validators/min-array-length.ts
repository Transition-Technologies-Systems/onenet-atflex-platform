import { AbstractControl, ValidationErrors } from '@angular/forms';

export const minArrayLength = (min: number) => {
  return (control: AbstractControl): ValidationErrors | null => {
    if (control.value.length >= min) return null;

    return { minlength: true };
  };
};
