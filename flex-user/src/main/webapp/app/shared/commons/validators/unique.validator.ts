import { first } from 'rxjs/operators';

import { UntypedFormControl, ValidatorFn } from '@angular/forms';

export class UniqueValidator {
  static addError(control: UntypedFormControl, currentValidators: ValidatorFn[]): void {
    control.setErrors({ unique: true });
    control.setValidators([...currentValidators, NotUniqueValidator]);

    control.valueChanges.pipe(first()).subscribe(() => {
      control.setValidators(currentValidators);
      control.setErrors({ unique: null });
      control.updateValueAndValidity();
    });
  }
}

function NotUniqueValidator() {
  return { unique: true };
}
