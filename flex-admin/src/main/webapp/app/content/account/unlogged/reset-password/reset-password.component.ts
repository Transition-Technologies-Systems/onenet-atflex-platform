import { ActivatedRoute, Router } from '@angular/router';
import { Component, OnInit } from '@angular/core';
import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';

import { AccountService } from '../account.service';
import { AppToastrService } from '@app/core';
import { RequiredNoWhitespaceValidator } from '@app/shared/commons/validators';
import { catchError, finalize } from 'rxjs/operators';
import { matchValues } from '@app/shared/validators';

@Component({
  selector: 'app-reset-password',
  templateUrl: './reset-password.component.html',
})
export class ResetPasswordComponent implements OnInit {
  form = this.createForm();

  private key: string | undefined;

  constructor(
    private router: Router,
    private fb: UntypedFormBuilder,
    private route: ActivatedRoute,
    private service: AccountService,
    private toastrService: AppToastrService
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(data => {
      this.key = data.key;
    });
  }

  goToLogin(): void {
    this.router.navigate(['/login']);
  }

  resetPassword(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const data = {
      ...this.form.getRawValue(),
      key: this.key,
    };

    this.service
      .resetPassword(data)
      .pipe(
        catchError((): any => {
          this.toastrService.error('account.resetPassword.error');
        }),
        finalize(() => this.goToLogin())
      )
      .subscribe(() => this.toastrService.success('account.resetPassword.success'),
      );
  }

  private createForm(): UntypedFormGroup {
    return this.fb.group({
      password: [
        null,
        [RequiredNoWhitespaceValidator, matchValues('confirmPassword', true), Validators.minLength(4), Validators.maxLength(100)],
      ],
      confirmPassword: [null, [RequiredNoWhitespaceValidator, matchValues('password')]],
    });
  }
}
