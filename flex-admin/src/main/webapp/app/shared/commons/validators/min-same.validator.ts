import { AbstractControl, ValidatorFn } from '@angular/forms';
import { Helpers } from '../helpers';

export function MinOrSameValidator(value: number): ValidatorFn {
  return (control: AbstractControl): { [key: string]: any } | null => {
    if (Helpers.isNill(control.value)) {
      return null;
    }

    if (value !== undefined && control.value < value) {
      return {
        min: true,
      };
    }

    return null;
  };
}

export function MinOrSameControlValidator(minFieldControlName: string): ValidatorFn {
  return (control: AbstractControl): { [key: string]: any } | null => {
    if (Helpers.isNill(control.value)) {
      return null;
    }

    const controlField = control.parent?.get(minFieldControlName);

    if (!controlField) {
      return null;
    }

    const valueMin = controlField.value;

    if (Helpers.isNill(valueMin)) {
      return null;
    }

    if (control.value !== undefined && control.value < valueMin) {
      return {
        minControl: true,
      };
    }

    return null;
  };
}
