import { catchError } from 'rxjs';
import * as AuthActions from '@app/core/auth/actions';

import { AppToastrService, State } from '@app/core';
import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';

import { AccountService } from '../account.service';
import { Component } from '@angular/core';
import { RequiredNoWhitespaceValidator } from '@app/shared/commons/validators';
import { Router } from '@angular/router';
import { Store } from '@ngrx/store';
import { matchValues } from '@app/shared/validators';

@Component({
  selector: 'app-change-password',
  templateUrl: './change-password.component.html',
})
export class ChangePasswordComponent {
  form = this.createForm();

  constructor(
    private router: Router,
    private fb: UntypedFormBuilder,
    private store: Store<State>,
    private service: AccountService,
    private toastrService: AppToastrService
  ) {}

  changePassword(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.service
      .changePassword(this.form.getRawValue())
      .pipe(
        catchError((): any => {
          this.toastrService.error('account.changePassword.error');
        })
      )
      .subscribe(() => {
          this.toastrService.success('account.changePassword.success');
          this.store.dispatch(AuthActions.changePassword());
          this.router.navigate(['/']);
        },
      );
  }

  private createForm(): UntypedFormGroup {
    return this.fb.group({
      currentPassword: [null, RequiredNoWhitespaceValidator],
      password: [
        null,
        [RequiredNoWhitespaceValidator, matchValues('confirmPassword', true), Validators.minLength(4), Validators.maxLength(100)],
      ],
      confirmPassword: [null, [RequiredNoWhitespaceValidator, matchValues('password')]],
    });
  }
}
