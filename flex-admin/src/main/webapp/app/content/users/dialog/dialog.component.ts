import * as moment from 'moment';

import { Component, OnInit } from '@angular/core';
import { CountryISO, SearchCountryField } from 'ngx-intl-tel-input';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { Observable, of, catchError } from 'rxjs';

import { AppToastrService } from '@app/core';
import { DialogExtends } from '@app/shared';
import { Dictionary } from '@app/shared/models';
import { Helpers } from '@app/shared/commons';
import { HttpErrorResponse } from '@angular/common/http';
import { Role } from '@app/shared/enums';
import { UsersDialogService } from './dialog.service';
import { UsersService } from '../users.service';
import { Validators } from '@angular/forms';
import { takeUntil } from 'rxjs/operators';

interface Dictionaries {
  roles: Dictionary[];
  companies$: Observable<Dictionary[]>;
}

@Component({
  selector: 'app-users-dialog',
  templateUrl: './dialog.component.html',
  providers: [UsersDialogService],
})
export class UsersDialogComponent extends DialogExtends implements OnInit {
  fspOwner = false;
  selectedCountry = CountryISO.Poland;
  prefferedCountries = [CountryISO.Poland];
  searchCountryField = [SearchCountryField.All];
  form = this.service.createForm(this.config.data, this.mode);

  dictionaries: Dictionaries = {
    roles: Helpers.enumToDictionary(Role, 'Role'),
    companies$: of([]),
  };

  get isFSP(): boolean {
    return [Role.ROLE_FLEX_SERVICE_PROVIDER, Role.ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED, Role.ROLE_BALANCING_SERVICE_PROVIDER].includes(
      this.form.get('role')?.value
    );
  }

  get fspLabel(): string {
    switch (this.form.get('role')?.value) {
      case Role.ROLE_BALANCING_SERVICE_PROVIDER:
        return 'users.form.bsp';
      case Role.ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED:
        return 'users.form.fspa';
    }

    return 'users.form.fspId';
  }

  get lastSuccessfulLoginDate(): string {
    if (!this.config.data?.lastSuccessfulLoginDate) {
      return '-';
    }

    return moment(this.config.data.lastSuccessfulLoginDate).format('DD/MM/yyyy HH:mm:ss');
  }

  get unsuccessfulLoginCount(): number {
    return this.config.data?.unsuccessfulLoginCount || 0;
  }

  constructor(
    public ref: DynamicDialogRef,
    public toastr: AppToastrService,
    public config: DynamicDialogConfig,
    private usersService: UsersService,
    private service: UsersDialogService
  ) {
    super(ref, config);

    this.fspOwner = !!this.config.data?.fspOwner;
  }

  ngOnInit(): void {
    this.subscribePasswordSetByUser();
    this.subscribeRole();

    if (this.isFSP) {
      const role = this.form.get('role')?.value;

      this.dictionaries.companies$ = this.service.getCompanies(role);
    }
  }

  save(): void {
    let method: Observable<void>;

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.toastr.warning('invalidForm');

      return;
    }

    const { passwordSetByUser, role, ...formData } = this.form.getRawValue();

    const data = {
      ...formData,
      roles: [role],
      password: passwordSetByUser ? null : formData.password,
    };

    if (this.mode === 'add') {
      method = this.usersService.save(data);
    } else {
      method = this.usersService.update(this.config.data.id, data);
    }

    method
      .pipe(
        catchError((response: HttpErrorResponse): any => {
          if (response.status === 400 && (response.error?.fieldErrors?.length || response.error?.errorKey)) {
            return;
          }

          this.toastr.error(`users.actions.${this.mode}.error`);
        })
      )
      .subscribe(() => {
        this.toastr.success(`users.actions.${this.mode}.success`);
        this.close(true);
      });
  }

  private subscribePasswordSetByUser(): void {
    this.form
      .get('passwordSetByUser')
      ?.valueChanges.pipe(takeUntil(this.destroy$))
      .subscribe((value: boolean) => {
        if (value) {
          this.form.get('password')?.disable();
          this.form.get('confirmPassword')?.disable();
        } else {
          this.form.get('password')?.enable();
          this.form.get('confirmPassword')?.enable();
        }
      });
  }

  private subscribeRole(): void {
    this.form
      .get('role')
      ?.valueChanges.pipe(takeUntil(this.destroy$))
      .subscribe((role: Role) => {
        const control = this.form.get('fspId');

        if (!control) {
          return;
        }

        if (this.isFSP) {
          this.dictionaries.companies$ = this.service.getCompanies(role);

          control.setValidators(Validators.required);
        } else {
          control.clearValidators();
        }

        control.updateValueAndValidity();
      });
  }
}
