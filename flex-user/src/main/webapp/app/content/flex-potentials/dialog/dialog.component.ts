import { AppToastrService } from '@app/core';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CountryISO, SearchCountryField } from 'ngx-intl-tel-input';
import { Dictionary, FlexPotentialDTO, ProductDTO } from '@app/shared/models';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { UntypedFormGroup, Validators } from '@angular/forms';
import { MaxOrSameValidator, MinOrSameValidator } from '@app/shared/commons/validators';
import { VolumeUnit } from '@app/shared/enums';

import { DialogExtends } from '@app/shared';
import { FlexPotentialsDialogService } from './dialog.service';
import { FlexPotentialsService } from '../flex-potentials.service';
import { Helpers } from '@app/shared/commons';
import { HttpErrorResponse } from '@angular/common/http';
import { Observable, catchError } from 'rxjs';
import { moment } from 'polyfills';
import { takeUntil } from 'rxjs/operators';

interface Dictionaries {
  products: Array<Dictionary & { minBidSize: number; maxBidSize: number }>;
  companies$: Observable<Dictionary[]>;
  volumeUnits: Dictionary[];
  units: Dictionary[];
}

@Component({
  selector: 'app-flex-potentials-dialog',
  templateUrl: './dialog.component.html',
  providers: [FlexPotentialsDialogService],
})
export class FlexPotentialsDialogComponent extends DialogExtends implements OnInit {
  form: UntypedFormGroup | undefined;

  minValidDate = moment(this.config.data?.createdDate).set({ m: 0 }).toDate();
  minDateTo = moment(this.minValidDate).add(1, 'h').toDate();

  productData: ProductDTO | undefined;

  selectedCountry = CountryISO.Poland;
  prefferedCountries = [CountryISO.Poland];
  searchCountryField = [SearchCountryField.All];

  dictionaries: Dictionaries = {
    companies$: this.service.getCompanies(),
    volumeUnits: Helpers.enumToDictionary(VolumeUnit, 'VolumeUnit'),
    products: [],
    units: [],
  };
  data: Partial<FlexPotentialDTO> = {};

  selectedFiles: File[] = [];
  removeFiles: number[] = [];

  get selectedProduct(): (Dictionary & { minBidSize: number; maxBidSize: number }) | undefined {
    const productId = this.form?.get('productId')?.value;
    const product = this.dictionaries.products.find(({ value }) => value === productId);

    return product;
  }

  get minBidSize(): number {
    return this.selectedProduct?.minBidSize as number;
  }

  get maxBidSize(): number {
    return this.selectedProduct?.maxBidSize as number;
  }

  get maxFullActivationTime(): number {
    return this.productData?.maxFullActivationTime as number;
  }

  get minRequiredDeliveryDuration(): number {
    return this.productData?.minRequiredDeliveryDuration as number;
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
    private flexPotentialsService: FlexPotentialsService,
    private service: FlexPotentialsDialogService
  ) {
    super(ref, config);
  }

  ngOnInit(): void {
    if (this.mode === 'edit') {
      this.flexPotentialsService.getFlexPotential(this.config.data.id).subscribe((response: FlexPotentialDTO) => {
        this.minValidDate = moment(response.createdDate).set({ m: 0 }).toDate();
        this.form = this.service.createForm(response, this.mode);
        this.data = response;

        this.getProduct(response?.product?.id);
        this.initSubscribe();
      });
    } else {
      this.form = this.service.createForm(this.config.data, this.mode);

      this.initSubscribe();
    }

    this.getProducts();
  }

  onChangeFileSelected(files: File[]): void {
    this.selectedFiles = files;
  }

  onDownloadFile(id: number): void {
    this.flexPotentialsService.downloadFile(id);
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

    const currentData = this.data || this.config.data;

    const { validFrom, validTo, ...formData } = this.form.getRawValue();

    const data = {
      ...currentData,
      ...formData,
      removeFiles: this.removeFiles,
      validTo: moment(validTo).set({ m: 0, s: 0, ms: 0 }),
      validFrom: moment(validFrom).set({ m: 0, s: 0, ms: 0 }),
    };

    if (this.mode === 'add') {
      method = this.flexPotentialsService.save(data, this.selectedFiles);
    } else {
      method = this.flexPotentialsService.update(this.config.data.id, data, this.selectedFiles);
    }

    method
      .pipe(
        catchError((response: HttpErrorResponse): any => {
          if (!(response.status === 400 && response.error?.errorKey)) {
            this.toastr.error(`flexPotentials.actions.${this.mode}.error`);
            return;
          }
        })
      )
      .subscribe(() => {
        this.toastr.success(`flexPotentials.actions.${this.mode}.success`);
        this.close(true);
      });
  }

  private getProduct(id: number): void {
    if (!id) {
      this.productData = undefined;
      return;
    }

    this.service.getProduct(id).subscribe((response: ProductDTO) => {
      this.productData = response;

      if (this.mode === 'add') {
        this.form?.get('fullActivationTime')?.setValue(response.maxFullActivationTime);
        this.form?.get('minDeliveryDuration')?.setValue(response.minRequiredDeliveryDuration);
      }

      this.form
        ?.get('volume')
        ?.setValidators([Validators.required, MinOrSameValidator(response.minBidSize), MaxOrSameValidator(response.maxBidSize)]);
      this.form?.get('fullActivationTime')?.setValidators([Validators.required, MaxOrSameValidator(this.maxFullActivationTime)]);
      this.form?.get('minDeliveryDuration')?.setValidators([Validators.required, MinOrSameValidator(this.minRequiredDeliveryDuration)]);

      this.form?.get('volumeUnit')?.setValue(response.bidSizeUnit);

      this.cdr.markForCheck();
    });
  }

  private getProducts(): void {
    this.flexPotentialsService.getProducts().subscribe(response => {
      this.dictionaries.products = response || [];
    });
  }

  private getUnits(): void {
    this.flexPotentialsService.getUnits(this.data.id).subscribe(response => {
      this.dictionaries.units = response || [];
    });
  }

  private initSubscribe(): void {
    this.subscribeDateTo();
    this.subscribeDateFrom();
    this.subscribeProduct();

    this.getUnits();
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

  private subscribeDateFrom(): void {
    this.form
      ?.get('validFrom')
      ?.valueChanges.pipe(takeUntil(this.destroy$))
      .subscribe((date: Date) => {
        const disabledActive = date ? moment(date).isAfter(moment()) : false;
        const control = this.form?.get('active');

        if (disabledActive) {
          control?.disable();
          control?.setValue(false);
        } else {
          control?.enable();
        }
      });
  }

  private subscribeProduct(): void {
    this.form
      ?.get('productId')
      ?.valueChanges.pipe(takeUntil(this.destroy$))
      .subscribe((productId: number) => this.getProduct(productId));
  }
}
