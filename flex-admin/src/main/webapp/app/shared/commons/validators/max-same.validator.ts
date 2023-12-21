import { AbstractControl, ValidatorFn } from '@angular/forms';
import { Helpers } from '../helpers';

export function MaxOrSameValidator(value: number | undefined): ValidatorFn {
  return (control: AbstractControl): { [key: string]: any } | null => {
    if (Helpers.isNill(control.value)) {
      return null;
    }

    if (value !== undefined && control.value > value) {
      return {
        max: true,
      };
    }

    return null;
  };
}

export function MaxOrSameControlValidator(maxFieldControlName: string): ValidatorFn {
  return (control: AbstractControl): { [key: string]: any } | null => {
    if (Helpers.isNill(control.value)) {
      return null;
    }

    const controlField = control.parent?.get(maxFieldControlName);

    if (!controlField) {
      return null;
    }

    const valueMax = controlField.value;

    if (Helpers.isNill(valueMax)) {
      return null;
    }

    if (control.value !== undefined && control.value > valueMax) {
      return {
        maxControl: true,
      };
    }

    return null;
  };
}
