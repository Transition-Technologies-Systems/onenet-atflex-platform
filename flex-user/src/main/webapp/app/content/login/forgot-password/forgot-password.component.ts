import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';

import { AppToastrService } from '@app/core';
import { Component } from '@angular/core';
import { LoginService } from '../login.service';
import { finalize, catchError } from 'rxjs/operators';

@Component({
  selector: 'app-forgot-password',
  templateUrl: './forgot-password.component.html',
})
export class ForgotPasswordComponent {
  form = this.createForm();

  constructor(
    private fb: UntypedFormBuilder,
    public ref: DynamicDialogRef,
    private service: LoginService,
    public config: DynamicDialogConfig,
    private toastrService: AppToastrService
  ) {}

  forgotPassword(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.service
      .forgotPassword(this.form.get('email')?.value)
      .pipe(
        catchError((): any => this.toastrService.error('login.forgotPassword.error')),
        finalize(() => this.ref.close())
      )
      .subscribe(() => this.toastrService.success('login.forgotPassword.success'));
  }

  private createForm(): UntypedFormGroup {
    return this.fb.group({
      email: [null, [Validators.required, Validators.email]],
    });
  }
}
