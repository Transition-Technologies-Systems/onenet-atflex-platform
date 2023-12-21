import { Directive, Input, OnDestroy, OnInit } from '@angular/core';
import { GroupValidatorError, ValidatorError, ValidatorService } from './validator.service';
import { distinctUntilChanged, filter } from 'rxjs/operators';

import { UntypedFormGroup } from '@angular/forms';
import { Subscription } from 'rxjs';

@Directive({
  selector: '[appValidatorContainer]',
})
export class ValidatorContainerDirective implements OnInit, OnDestroy {
  @Input('appValidatorContainer') objectName = 'default';
  @Input() form: UntypedFormGroup | undefined;

  private subscription = new Subscription();
  private fieldSubscriptions: Record<string, Subscription | null> = {};

  constructor(private service: ValidatorService) {}

  ngOnInit(): void {
    this.subscribeValidatorErrors();
  }

  ngOnDestroy(): void {
    this.unsubscribeField();
    this.subscription.unsubscribe();
  }

  private getSubscriptionKey(field: string): string {
    return field.split('.').join('-');
  }

  private subscribeValidatorErrors(): void {
    this.subscription.add(
      this.service.validatorErrors$
        .asObservable()
        .pipe(
          filter((validators: GroupValidatorError[]) => validators.map(({ objectName }) => objectName).includes(this.objectName)),
          distinctUntilChanged()
        )
        .subscribe((validators: GroupValidatorError[]) => {
          this.preparedFieldSubscriptions(validators.flatMap(({ errors }) => errors));
        })
    );
  }

  private preparedFieldSubscriptions(errors: Omit<ValidatorError, 'objectName'>[]): void {
    this.unsubscribeField();

    errors.forEach((error: Omit<ValidatorError, 'objectName'>) => {
      if (!error.fields) {
        return;
      }

      error.fields.forEach((field: string) => {
        const subscriptionKey = this.getSubscriptionKey(field);
        const control = this.form?.get(field);

        if (!control) {
          return;
        }

        this.fieldSubscriptions[subscriptionKey] = control.valueChanges.subscribe(val => {
          this.fieldSubscriptions[subscriptionKey]?.unsubscribe();
          this.fieldSubscriptions[subscriptionKey] = null;

          this.service.clearValidator(this.objectName, field);
        });
      });
    });
  }

  private unsubscribeField(): void {
    Object.entries(this.fieldSubscriptions).forEach(([key, subscription]) => {
      if (!subscription) {
        return;
      }

      subscription.unsubscribe();
    });
  }
}
