import * as moment from 'moment';

import { AppToastrService, AuthService } from '@app/core';
import { Component, OnInit } from '@angular/core';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { LocalizationType, Role } from '@app/shared/enums';
import { Observable, of, catchError } from 'rxjs';

import { DialogExtends } from '@app/shared';
import { Dictionary } from '@app/shared/models';
import { DictionaryLangDto } from '@app/content/dictionaries/dictionaries';
import { UntypedFormGroup } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { SubportfolioDTO } from '../subportfolio';
import { SubportfoliosDialogService } from './dialog.service';
import { SubportfoliosService } from '../subportfolio.service';
import { takeUntil } from 'rxjs/operators';

interface Dictionaries {
  units: Dictionary[];
  companies$: Observable<Dictionary[]>;
  localizationTypes$: Observable<DictionaryLangDto[]>;
}

@Component({
  selector: 'app-subportfolio-dialog',
  templateUrl: './dialog.component.html',
  providers: [SubportfoliosDialogService],
})
export class SubportfoliosDialogComponent extends DialogExtends implements OnInit {
  form: UntypedFormGroup | undefined;
  data: Partial<SubportfolioDTO> = {};

  currentDate = moment().set({ m: 0, s: 0, ms: 0 }).toDate();
  minDateTo = moment(this.currentDate).add(1, 'h').toDate();

  dictionaries: Dictionaries = {
    localizationTypes$: this.subportfolioService.getLocalizationsDict(LocalizationType.COUPLING_POINT_ID),
    companies$: of([]),
    units: [],
  };

  isAdmin = false;
  selectedFiles: File[] = [];
  removeFiles: number[] = [];

  get minValidTo(): Date {
    const validFrom = this.form?.get('validFrom')?.value;

    if (!validFrom) {
      return this.minDateTo;
    }

    return moment(validFrom).isBefore(moment(this.minDateTo)) ? this.minDateTo : validFrom;
  }

  constructor(
    public ref: DynamicDialogRef,
    public toastr: AppToastrService,
    private authService: AuthService,
    public config: DynamicDialogConfig,
    private subportfolioService: SubportfoliosService,
    private service: SubportfoliosDialogService
  ) {
    super(ref, config);
  }

  ngOnInit(): void {
    if (this.mode === 'edit') {
      this.subportfolioService.getSubportfolio(this.config.data.id).subscribe((response: SubportfolioDTO) => {
        this.createForm(response);
        this.data = response;

        this.getUnits(this.data.fspId);
      });
    } else {
      this.createForm(this.config.data);
    }
  }

  onChangeFileSelected(files: File[]): void {
    this.selectedFiles = files;
  }

  onDownloadFile(id: number): void {
    this.subportfolioService.downloadFile(id);
  }

  onRemoveFileChange(ids: number[]): void {
    this.removeFiles = ids;
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
      ...formData,
      removeFiles: this.removeFiles,
      validTo: moment(validTo).set({ m: 0, s: 0, ms: 0 }),
      validFrom: moment(validFrom).set({ m: 0, s: 0, ms: 0 }),
    };

    if (this.mode === 'add') {
      method = this.subportfolioService.save(data, this.selectedFiles);
    } else {
      method = this.subportfolioService.update(this.config.data.id, data, this.selectedFiles);
    }

    method
      .pipe(
        catchError((response: HttpErrorResponse): any => {
          if (response.status === 400 && (response.error?.fieldErrors?.length || response.error?.errorKey)) {
            return;
          }

          this.toastr.error(`subportfolio.actions.${this.mode}.error`);
        })
      )
      .subscribe(() => {
        this.toastr.success(`subportfolio.actions.${this.mode}.success`);
        this.close(true);
      });
  }

  private createForm(data: SubportfolioDTO): void {
    this.authService.hasRole(Role.ROLE_ADMIN).then((hasRole: boolean) => {
      this.isAdmin = hasRole;
    });

    this.authService
      .hasAnyRoles([Role.ROLE_ADMIN, Role.ROLE_DISTRIBUTION_SYSTEM_OPERATOR, Role.ROLE_TRANSMISSION_SYSTEM_OPERATOR])
      .then((hasRole: boolean) => {
        this.form = this.service.createForm(data, hasRole);
        this.initSubscribe();
      });
  }

  private getUnits(fspId: number | undefined): void {
    if (!fspId) {
      return;
    }

    this.service.getUnits(fspId, this.data?.id).subscribe(response => {
      this.dictionaries.units = response || [];
    });
  }

  private initSubscribe(): void {
    const fspId = this.form?.get('fspId')?.value;
    this.dictionaries.companies$ = this.service.getCompanies(Array.isArray(fspId) ? fspId : [fspId], this.data);

    this.subscribeDateTo();
    this.subscribeDateFrom();
    this.subscribeFspId();
  }

  private subscribeDateTo(): void {
    this.form
      ?.get('validTo')
      ?.valueChanges.pipe(takeUntil(this.destroy$))
      .subscribe((date: Date) => {
        const disabledActive = date ? moment(date).isSameOrBefore(moment()) : false;
        const control = this.form?.get('active');

        if (!this.isAdmin) {
          return;
        }

        if (disabledActive) {
          control?.disable();
          control?.setValue(false);
        } else {
          control?.enable();
        }
      });
  }

  private subscribeDateFrom(): void {
    this.form
      ?.get('validFrom')
      ?.valueChanges.pipe(takeUntil(this.destroy$))
      .subscribe((date: Date) => {
        const disabledActive = date ? moment(date).isAfter(moment()) : false;
        const control = this.form?.get('active');

        if (!this.isAdmin) {
          return;
        }

        if (disabledActive) {
          control?.disable();
          control?.setValue(false);
        } else {
          control?.enable();
        }
      });
  }

  private subscribeFspId(): void {
    this.form
      ?.get('fspId')
      ?.valueChanges.pipe(takeUntil(this.destroy$))
      .subscribe((fspId: number) => {
        if (!this.isAdmin) {
          return;
        }

        if (!fspId) {
          this.dictionaries.units = [];
          this.form?.get('unitId')?.disable();
          return;
        }

        this.getUnits(fspId);

        this.form?.get('unitId')?.enable();
      });
  }
}
