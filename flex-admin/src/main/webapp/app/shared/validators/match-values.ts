import { AbstractControl, ValidationErrors } from '@angular/forms';

export const matchValues = (matchTo: string, reverse: boolean = false): ((control: AbstractControl) => ValidationErrors | null) => {
  return (control: AbstractControl): ValidationErrors | null => {
    if (!control.parent) {
      return null;
    }

    const controlParents = (control.parent?.controls || {}) as { [key: string]: AbstractControl };

    const isSame = control.value === controlParents[matchTo].value;

    if (reverse) {
      controlParents[matchTo].setErrors(isSame ? null : { isMatching: true });
    } else {
      return isSame ? null : { isMatching: true };
    }

    return null;
  };
};
