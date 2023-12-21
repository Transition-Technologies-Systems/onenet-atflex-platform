import { AbstractControl } from '@angular/forms';

export function RequiredNoWhitespaceValidator(control: AbstractControl) {
  if (
    control.value === null ||
    (!control.value && typeof control.value !== 'number') ||
    (typeof control.value === 'string' && control.value.trim() === '')
  ) {
    return {
      required: true,
    };
  }

  return null;
}
