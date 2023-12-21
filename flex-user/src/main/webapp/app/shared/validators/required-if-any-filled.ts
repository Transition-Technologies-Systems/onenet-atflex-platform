import { AbstractControl, ValidationErrors } from '@angular/forms';

import { Helpers } from '../commons';

export const requiredIfAnyFilled = (
  someFilledKeys: string[],
  clearErrorWhenFilled: string[]
): ((control: AbstractControl) => ValidationErrors | null) => {
  return (control: AbstractControl): ValidationErrors | null => {
    if (!control.parent) {
      return null;
    }

    const controlParents = (control.parent?.controls || {}) as { [key: string]: AbstractControl };

    const someFilled =
      someFilledKeys.length === 0 ? false : someFilledKeys.some((key: string) => Helpers.isFilled(controlParents[key]?.value));

    if (!someFilled) {
      const keys = clearErrorWhenFilled ? clearErrorWhenFilled : someFilledKeys;

      keys.forEach((key: string) => controlParents[key]?.setErrors({ requiredOne: true }));

      return {
        requiredOne: true,
      };
    } else {
      const keys = clearErrorWhenFilled ? clearErrorWhenFilled : someFilledKeys;

      keys.forEach((key: string) => controlParents[key]?.setErrors(null));

      return null;
    }
  };
};
