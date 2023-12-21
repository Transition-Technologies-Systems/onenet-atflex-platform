import { AbstractControl, ValidatorFn } from '@angular/forms';

export function UpdateValueAndValidity(controlName: string | string[]): ValidatorFn {
  let prevValue: any = undefined;

  return (control: AbstractControl): { [key: string]: any } | null => {
    const fields = Array.isArray(controlName) ? controlName : [controlName];
    const value = control.value;

    fields.forEach((field: string) => {
      const controlField = control.parent?.get(field);

      if (prevValue !== value) {
        prevValue = value;
        controlField?.updateValueAndValidity({ onlySelf: true, emitEvent: false });
      }
    });

    return null;
  };
}
