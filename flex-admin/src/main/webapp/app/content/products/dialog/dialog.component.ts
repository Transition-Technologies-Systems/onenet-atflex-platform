import * as moment from 'moment';

import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { Dictionary, ProductDTO } from '@app/shared/models';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { Observable, distinctUntilChanged, takeUntil, catchError } from 'rxjs';
import { ProductDirection, VolumeUnit } from '@app/shared/enums';

import { AppToastrService } from '@app/core';
import { DialogExtends } from '@app/shared';
import { FileData } from '@app/shared/commons/file-upload';
import { UntypedFormGroup } from '@angular/forms';
import { Helpers } from '@app/shared/commons';
import { HttpErrorResponse } from '@angular/common/http';
import { ProductsDialogService } from './dialog.service';
import { ProductsService } from '../products.service';

interface Dictionaries {
  users: Dictionary[];
  units: Dictionary[];
  directions: Dictionary[];
}

@Component({
  selector: 'app-products-dialog',
  templateUrl: './dialog.component.html',
  providers: [ProductsDialogService],
})
export class ProductsDialogComponent extends DialogExtends implements OnInit {
  form: UntypedFormGroup | undefined;

  currentDate = moment().set({ m: 0, s: 0, ms: 0 }).toDate();
  minDateTo = moment(this.currentDate).add(1, 'h').toDate();

  selectedFiles: File[] = [];
  removeFiles: number[] = [];
  data: Partial<ProductDTO> = {};

  dictionaries: Dictionaries = {
    users: [],
    units: Helpers.enumToDictionary(VolumeUnit, 'VolumeUnit'),
    directions: Helpers.enumToDictionary(ProductDirection, 'Direction'),
  };

  get maxBidSize(): number {
    const controlValue = this.form?.get('maxBidSize')?.value;

    if (controlValue || controlValue === 0) {
      return controlValue;
    }

    return 9999.99;
  }

  get minBidSize(): number {
    const controlValue = this.form?.get('minBidSize')?.value;

    if (controlValue || controlValue === 0) {
      return controlValue;
    }

    return -9999.99;
  }

  get minValidTo(): Date {
    const validFrom = this.form?.get('validFrom')?.value;

    if (!validFrom) {
      return this.minDateTo;
    }

    return moment(validFrom).isBefore(moment(this.minDateTo)) ? this.minDateTo : validFrom;
  }

  constructor(
    public ref: DynamicDialogRef,
    public cdr: ChangeDetectorRef,
    public toastr: AppToastrService,
    public config: DynamicDialogConfig,
    private productsService: ProductsService,
    private service: ProductsDialogService
  ) {
    super(ref, config);
  }

  ngOnInit(): void {
    this.getDictionaries();

    if (this.mode === 'edit') {
      this.productsService.getProduct(this.config.data.id).subscribe((response: ProductDTO) => {
        this.form = this.service.createForm(response, this.mode);
        this.data = response;

        this.initSubscribe();
      });
    } else {
      this.form = this.service.createForm(this.config.data, this.mode);

      this.initSubscribe();
    }
  }

  getFiles(): Array<FileData | File> {
    const files = this.data.filesMinimal || [];
    return files;
  }

  onChangeFileSelected(files: File[]): void {
    this.selectedFiles = files;
  }

  onDownloadFile(id: number): void {
    this.productsService.downloadFile(id);
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
      method = this.productsService.save(data, this.selectedFiles);
    } else {
      method = this.productsService.update(data, this.selectedFiles);
    }

    method
      .pipe(
        catchError((response: HttpErrorResponse): any => {
          if (response.status === 400 && (response.error?.fieldErrors?.length || response.error?.errorKey)) {
            return;
          }
          this.toastr.error(`products.actions.${this.mode}.error`);
        })
      )
      .subscribe(() => {
        this.toastr.success(`products.actions.${this.mode}.success`);
        this.close(true);
      });
  }

  private getDictionaries(): void {
    this.productsService.getUsers().subscribe((response: Dictionary[]) => {
      this.dictionaries.users = response;
      this.cdr.markForCheck();
    });
  }

  private initSubscribe(): void {
    this.subscribeDateTo();
    this.subscribeBalancingAndCmvc();
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

  private subscribeBalancingAndCmvc(): void {
    this.form
      ?.get('balancing')
      ?.valueChanges?.pipe(distinctUntilChanged())
      ?.subscribe(() => {
        this.form?.get('cmvc')?.updateValueAndValidity();
      });

    this.form
      ?.get('cmvc')
      ?.valueChanges?.pipe(distinctUntilChanged())
      ?.subscribe(() => {
        this.form?.get('balancing')?.updateValueAndValidity();
      });
  }
}
