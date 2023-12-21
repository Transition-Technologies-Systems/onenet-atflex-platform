import { AbstractControl, ValidatorFn } from '@angular/forms';

import { Helpers } from '../helpers';

export function RequireOneValidator(keys: string[]): ValidatorFn {
  return (control: AbstractControl): { [key: string]: any } | null => {
    const parent = control.parent;

    const some = keys.some((key: string) => {
      const value = parent?.get(key)?.value;

      return value !== false && (value === true || !Helpers.isNill(value));
    });

    if (!some) {
      return {
        required: true,
      };
    }

    return null;
  };
}
