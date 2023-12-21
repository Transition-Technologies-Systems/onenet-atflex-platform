import { AppToastrService, ToastrMessage } from '@app/core';
import { Component, OnInit } from '@angular/core';
import { CountryISO, SearchCountryField } from 'ngx-intl-tel-input';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { EMPTY, Observable, takeUntil, catchError } from 'rxjs';

import { DialogExtends } from '@app/shared';
import { UntypedFormGroup } from '@angular/forms';
import { FspDTO } from '@app/shared/models';
import { FspsDialogService } from './dialog.service';
import { FspsService } from '../fsps.service';
import { HttpErrorResponse } from '@angular/common/http';
import { moment } from 'polyfills';

@Component({
  selector: 'app-fsps-dialog',
  templateUrl: './dialog.component.html',
  providers: [FspsDialogService],
})
export class FspsDialogComponent extends DialogExtends implements OnInit {
  form: UntypedFormGroup | undefined;
  minValidDate = moment(this.config.data?.model?.createdDate).set({ m: 0 }).toDate();

  roleName = 'FSP';
  selectedCountry = CountryISO.Poland;
  prefferedCountries = [CountryISO.Poland];
  searchCountryField = [SearchCountryField.All];
  minValidToDate = moment().toDate();

  data = { ...this.config.data.model, roleName: this.config.data.roleName };

  get isBsp(): boolean {
    return !!this.config.data.isBsp;
  }

  constructor(
    public ref: DynamicDialogRef,
    public toastr: AppToastrService,
    public config: DynamicDialogConfig,
    private fspsService: FspsService,
    private service: FspsDialogService
  ) {
    super(ref, config);

    this.mode = this.config.data?.model?.id ? 'edit' : 'add';
  }

  ngOnInit(): void {
    this.roleName = this.config.data.roleName;

    if (this.mode === 'edit') {
      this.fspsService.getFsp(this.config.data.model.id).subscribe((response: FspDTO) => {
        this.minValidDate = moment(response.createdDate).toDate();
        this.form = this.service.createForm(response);

        this.initSubscribe();
      });
    } else {
      this.form = this.service.createForm(this.config.data.model);

      this.initSubscribe();
    }
  }

  save(): void {
    let method: Observable<void>;

    if (!this.form) {
      return;
    }

    this.form.get('validFrom')?.updateValueAndValidity();

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.toastr.warning('invalidForm');
      return;
    }

    const { validFrom, validTo, ...formData } = this.form.getRawValue();

    const data = {
      ...this.data,
      ...formData,
      validTo: validTo ? moment(validTo).set({ m: 0, s: 0, ms: 0 }) : null,
      validFrom: validFrom ? moment(validFrom).set({ m: 0, s: 0, ms: 0 }) : null,
    };

    if (this.mode === 'add') {
      method = EMPTY;
    } else {
      method = this.fspsService.update(data);
    }

    method
      .pipe(
        catchError((response: HttpErrorResponse): any => {
          if (!(response.status === 400 && response.error?.errorKey)) {
            this.toastr.error(new ToastrMessage({ msg: `fsps.actions.${this.mode}.error`, params: { role: this.roleName } }));
            return;
          }
        })
      )
      .subscribe(() => {
        this.toastr.success(new ToastrMessage({ msg: `fsps.actions.${this.mode}.success`, params: { role: this.roleName } }));
        this.close(true);
      });
  }

  private getMinValidToDate(): void {
    const validFrom = this.form?.get('validFrom')?.value;

    this.minValidToDate = moment(validFrom).isBefore(moment()) ? moment().toDate() : validFrom;
  }

  private initSubscribe(): void {
    this.getMinValidToDate();

    this.subscribeDateFrom();
    this.subscribeDateTo();
  }

  private subscribeDateFrom(): void {
    this.form
      ?.get('validFrom')
      ?.valueChanges.pipe(takeUntil(this.destroy$))
      .subscribe(() => this.getMinValidToDate());
  }

  private subscribeDateTo(): void {
    this.form
      ?.get('validTo')
      ?.valueChanges.pipe(takeUntil(this.destroy$))
      .subscribe((date: Date) => {
        const disabledActive = date ? moment(date).isSameOrBefore(moment()) : false;
        const control = this.form?.get('active');

        if (disabledActive) {
          control?.disable();
          control?.setValue(false);
        } else {
          control?.enable();
        }
      });
  }
}
