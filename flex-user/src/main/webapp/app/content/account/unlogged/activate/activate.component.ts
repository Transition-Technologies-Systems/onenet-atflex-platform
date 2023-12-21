import { catchError } from 'rxjs';
import { ActivatedRoute, Router } from '@angular/router';
import { Component, OnInit } from '@angular/core';
import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';

import { AccountService } from '../account.service';
import { AppToastrService } from '@app/core';
import { FspUserRegistrationDTO } from '@app/shared/models';
import { HttpErrorResponse } from '@angular/common/http';
import { RequiredNoWhitespaceValidator } from '@app/shared/commons/validators';
import { matchValues } from '@app/shared/validators';

@Component({
  selector: 'app-registration-activate',
  templateUrl: './activate.component.html',
})
export class ActivateAccountComponent implements OnInit {
  form = this.createForm();
  data: FspUserRegistrationDTO | undefined;

  private key: string | undefined;

  constructor(
    private router: Router,
    private fb: UntypedFormBuilder,
    private route: ActivatedRoute,
    private service: AccountService,
    private toastr: AppToastrService
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(data => {
      this.key = data.key;

      this.getAccountData();
    });
  }

  activate(): void {
    if (!this.key) {
      return;
    }

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.service
      .activateAccount(this.key, this.form.get('password')?.value, this.form.get('login')?.value)
      .pipe(
        catchError((response: HttpErrorResponse): any => {
          if (!(response.status === 400 && response.error?.errorKey)) {
            this.toastr.error('account.activate.error');
            return;
          }
        })
      )
      .subscribe(() => {
        this.toastr.success('account.activate.success');
        this.router.navigate(['/login']);
      });
  }

  private createForm(): UntypedFormGroup {
    return this.fb.group({
      login: [null, [RequiredNoWhitespaceValidator]],
      password: [
        null,
        [RequiredNoWhitespaceValidator, matchValues('confirmPassword', true), Validators.minLength(4), Validators.maxLength(100)],
      ],
      confirmPassword: [null, [RequiredNoWhitespaceValidator, matchValues('password')]],

      firstName: [{ value: null, disabled: true }],
      lastName: [{ value: null, disabled: true }],
      companyName: [{ value: null, disabled: true }],
      email: [{ value: null, disabled: true }],
      phoneNumber: [{ value: null, disabled: true }],
    });
  }

  private getAccountData(): void {
    if (!this.key) {
      return;
    }

    this.service
      .getAccountDataByKey(this.key)
      .pipe(
        catchError((response: HttpErrorResponse) => {
          if (response.error?.errorKey === 'error.securityKeyIsInvalidOrExpired') {
            this.toastr.warning('error.activate.securityKeyIsInvalidOrExpired');
            this.router.navigate(['/login']);
          }
          throw response;
        })
      )
      .subscribe((data: FspUserRegistrationDTO) => {
        this.data = data;

        this.form.patchValue(data);
      });
  }
}
