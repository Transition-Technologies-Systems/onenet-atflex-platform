import { catchError } from 'rxjs';
import * as AuthActions from '@app/core/auth/actions';

import { AUTH_KEY, AppToastrService, LocalStorageService, State } from '@app/core';
import { UntypedFormBuilder, UntypedFormGroup } from '@angular/forms';

import { Component } from '@angular/core';
import { ForgotPasswordComponent } from './forgot-password';
import { HttpErrorResponse } from '@angular/common/http';
import { LoginService } from './login.service';
import { ModalService } from '@app/shared/commons';
import { RequiredNoWhitespaceValidator } from '@app/shared/commons/validators';
import { Router } from '@angular/router';
import { Store } from '@ngrx/store';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
})
export class LoginComponent {
  form = this.createForm();

  constructor(
    private router: Router,
    private fb: UntypedFormBuilder,
    private store: Store<State>,
    private service: LoginService,
    public toastr: AppToastrService,
    public modalService: ModalService,
    private localStorageService: LocalStorageService
  ) {}

  forgotPassword(): void {
    this.modalService.open(ForgotPasswordComponent);
  }

  goToRegister(): void {
    this.router.navigate(['/registration']);
  }

  logIn(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const { isAuthenticated } = this.localStorageService.getItem(AUTH_KEY) ?? {};

    if (isAuthenticated) {
      this.toastr.warning('login.alreadyLoggedIn', undefined, { life: 5000 });
      setTimeout(() => window.location.reload(), 4000);
      return;
    }

    this.service
      .login(this.form.getRawValue())
      .pipe(
        catchError((resp: HttpErrorResponse): any => {
          const errorMsg = resp.error?.msgKey;

          if (resp.error?.msgKey !== 'error.userIsNotActivated') {
            this.form.get('password')?.setErrors({ credentials: true });
          }

          if (!!errorMsg) {
            this.toastr.error(errorMsg);
          }
        })
      )
      .subscribe(({ id_token }: any) => {
        this.store.dispatch(AuthActions.login({ jwt: id_token }));
      });
  }

  private createForm(): UntypedFormGroup {
    return this.fb.group({
      username: [null, RequiredNoWhitespaceValidator],
      password: [null, RequiredNoWhitespaceValidator],
    });
  }
}
