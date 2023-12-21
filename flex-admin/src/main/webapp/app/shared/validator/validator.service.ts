import { AppToastrService, ToastrMessage } from '@app/core';
import { ApplicationRef, ComponentFactoryResolver, Injectable, Injector } from '@angular/core';

import { BehaviorSubject } from 'rxjs';
import { ValidatorComponent } from './validator.component';

export interface GroupValidatorError {
  objectName: string;
  errors: Omit<ValidatorError, 'objectName'>[];
}

export interface ValidatorError {
  objectName: string;
  message: string;
  fields: string[];
  field?: string;
}

export interface Validator {
  message: string;
  description: string;
  errors: ValidatorError[];
  fieldErrors: ValidatorError[];
}

@Injectable()
export class ValidatorService {
  validatorErrors$ = new BehaviorSubject<GroupValidatorError[]>([]);

  constructor(
    private injector: Injector,
    private appRef: ApplicationRef,
    private toastr: AppToastrService,
    private componentFactoryResolver: ComponentFactoryResolver
  ) {}

  clearValidator(objectName: string, fieldKey: string): void {
    const container = this.getContainerForObject(objectName);

    const { keys, indexes } = this.getFieldKey(fieldKey);
    const field = this.getField(keys, indexes, container);
    const [key] = keys;

    if (!field) {
      return;
    }

    field.classList.remove('validate-error');
    field.parentElement?.querySelector(`#${key}-validator`)?.remove();
  }

  showValidators(validator: Validator): void {
    const validators = this.groupValidatorErrors(validator);
    let toastrMsg: ToastrMessage[] = [];

    validators.forEach(({ objectName, errors }: GroupValidatorError) => {
      const container = this.getContainerForObject(objectName);

      errors.forEach((error: Omit<ValidatorError, 'objectName'>) => {
        const [errorMessage, errorParameters] = error.message.split(';');

        if (!error.fields.length) {
          toastrMsg = [
            ...toastrMsg,
            new ToastrMessage({
              msg: errorMessage,
              params: { value: errorParameters },
            }),
          ];
        }

        error.fields.forEach((fieldKey: string) => {
          const { keys, indexes } = this.getFieldKey(fieldKey);
          const field = this.getField(keys, indexes, container);

          if (!field) {
            return;
          }

          const node = this.getValidatorNode(field, keys);

          const componentFactory = this.componentFactoryResolver.resolveComponentFactory(ValidatorComponent);
          const ref = componentFactory.create(this.injector, [], node);
          ref.instance.message = errorMessage;
          ref.instance.parameters = { value: errorParameters };

          this.appRef.attachView(ref.hostView);

          field.classList.add('validate-error');
        });
      });
    });

    toastrMsg.forEach((message: ToastrMessage) => this.toastr.warning(message));

    const currentValidatorErrors = this.validatorErrors$.getValue();

    this.validatorErrors$.next([...currentValidatorErrors, ...validators]);
  }

  private getContainerForObject(objectName: string): HTMLElement | null {
    return document.querySelector(`[appvalidatorcontainer="${objectName}"]`);
  }

  private getField(fields: string[], indexes: string[], container: HTMLElement | null): HTMLElement | null {
    const [field = ''] = fields;
    const [index = null] = indexes;

    if (!container) {
      return null;
    }

    const fieldEl = container.querySelector(`#${field}`) as HTMLElement;

    if (index !== null && fieldEl) {
      return fieldEl.querySelector(`*[index="${index}"]`);
    }

    const closest = fieldEl.closest('.p-float-label') as HTMLElement;

    return closest || fieldEl;
  }

  private getFieldKey(field: string): { keys: string[]; field: string; indexes: string[] } {
    const regex = /\[*.]/gm;
    const indexesData = field.match(regex) || [];
    const indexes = indexesData.map((value: string) => value.replace(/[\[\]]/gm, ''));
    const fieldKeys = field.replace(regex, '').split('.');

    return {
      keys: fieldKeys.map((key: string) => `field-${key}`),
      indexes,
      field,
    };
  }

  private getValidatorNode(field: HTMLElement, keys: string[]): HTMLElement {
    let node: HTMLElement | null = null;
    const [key] = keys;
    const validatorId = `${key}-validator`;
    const validatorField = field.parentElement?.querySelector(`#${validatorId}`) as HTMLElement;

    if (validatorField) {
      node = validatorField;
    } else {
      node = document.createElement('div');
      node.id = validatorId;
      field.parentElement?.appendChild(node);
    }

    return node;
  }

  private groupValidatorErrors(validator: Validator): GroupValidatorError[] {
    return (validator.errors || validator.fieldErrors || []).reduce(
      (currentValue: GroupValidatorError[], { objectName, ...validatorError }: ValidatorError) => {
        const data: GroupValidatorError | undefined = currentValue.find(
          ({ objectName: currentObjectName }) => currentObjectName === objectName
        );

        if (!validatorError.fields && validatorError.field) {
          validatorError.fields = [validatorError.field];
        }

        if (data) {
          data.errors = [...data.errors, validatorError];

          return currentValue;
        }

        return [
          ...currentValue,
          {
            objectName,
            errors: [validatorError],
          },
        ];
      },
      []
    );
  }
}
