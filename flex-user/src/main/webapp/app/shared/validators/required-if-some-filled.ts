import { AbstractControl, ValidationErrors } from '@angular/forms';

import { Helpers } from '../commons';

export const requiredIfSomeFilled = (
  someFilledKeys: string[],
  setErrorWhenFilled: string[]
): ((control: AbstractControl) => ValidationErrors | null) => {
  return (control: AbstractControl): ValidationErrors | null => {
    if (!control.parent) {
      return null;
    }

    const controlParents = (control.parent?.controls || {}) as { [key: string]: AbstractControl };
    const controlValue = control.value;

    const someFilled =
      someFilledKeys.length === 0 ? false : someFilledKeys.some((key: string) => Helpers.isFilled(controlParents[key]?.value));

    if (Helpers.isFilled(controlValue) && (typeof controlValue !== 'boolean' || controlValue === true)) {
      const keys = setErrorWhenFilled ? setErrorWhenFilled : someFilledKeys;

      keys.forEach((key: string) => {
        if (!Helpers.isFilled(controlParents[key]?.value)) {
          controlParents[key]?.setErrors({ required: true });
        }
      });

      return null;
    } else {
      if (someFilled) {
        return { required: true };
      } else {
        const keys = setErrorWhenFilled ? setErrorWhenFilled : someFilledKeys;

        keys.forEach((key: string) => controlParents[key]?.setErrors(null));
      }
    }

    return null;
  };
};
