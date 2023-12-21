import { AppToastrService, ToastrMessage } from '@app/core';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { Dictionary, ProductDTO } from '@app/shared/models';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { Observable, takeUntil, catchError } from 'rxjs';
import { UntypedFormGroup } from '@angular/forms';
import { MaxOrSameValidator, MinOrSameValidator } from '@app/shared/commons/validators';

import { AuctionCmvcDTO } from '../cm-vc';
import { AuctionStatus } from '../../enums';
import { CmVcDialogService } from './dialog.service';
import { CmVcService } from '../cm-vc.service';
import { DialogExtends } from '@app/shared';
import { HttpErrorResponse } from '@angular/common/http';
import { LocalizationTypeDTO } from '@app/content/dictionaries/dictionaries';
import { moment } from 'polyfills';

interface Dictionaries {
  products$: Observable<Array<Dictionary & { minBidSize: number; maxBidSize: number }>>;
  localizationTypes$: Observable<LocalizationTypeDTO[]>;
}

@Component({
  selector: 'app-auctions-cmvc-dialog',
  templateUrl: './dialog.component.html',
  providers: [CmVcDialogService],
})
export class CmVcDialogComponent extends DialogExtends implements OnInit {
  minDate = moment().toDate();

  form: UntypedFormGroup | undefined;
  productData: ProductDTO | undefined;
  data: AuctionCmvcDTO = this.config.data;

  dictionaries: Dictionaries = {
    products$: this.auctionService.getProducts(),
    localizationTypes$: this.auctionService.getLocalizationsDict(),
  };

  get minBidSize(): number {
    return this.form?.get('product')?.value?.minBidSize as number;
  }

  get maxBidSize(): number {
    return this.form?.get('product')?.value?.maxBidSize as number;
  }

  get maxDesiredPower(): number {
    const controlValue = this.form?.get('maxDesiredPower')?.value;

    if (controlValue || controlValue === 0) {
      return controlValue;
    }

    if (!this.maxBidSize && this.maxBidSize !== 0) {
      return 9999.99;
    }

    return this.maxBidSize;
  }

  get minDesiredPower(): number {
    const controlValue = this.form?.get('minDesiredPower')?.value;

    if (controlValue || controlValue === 0) {
      return controlValue;
    }

    return this.minBidSize;
  }

  get isOpenOrClosedAuction(): boolean {
    return [AuctionStatus.OPEN, AuctionStatus.CLOSED].includes(this.data?.status);
  }

  constructor(
    private cdr: ChangeDetectorRef,
    public ref: DynamicDialogRef,
    public toastr: AppToastrService,
    public config: DynamicDialogConfig,
    private service: CmVcDialogService,
    private auctionService: CmVcService
  ) {
    super(ref, config);

    this.mode = this.config.data?.id ? 'edit' : 'add';
  }

  ngOnInit(): void {
    if (this.mode === 'edit') {
      this.auctionService.getAuction(this.config.data.id).subscribe((response: AuctionCmvcDTO) => {
        this.form = this.service.createForm(response);
        this.data = response;

        this.getProduct(this.data.product?.id);

        const minDesiredPowerControl = this.form?.get('minDesiredPower');
        const maxDesiredPowerControl = this.form?.get('maxDesiredPower');

        minDesiredPowerControl?.setValidators(MinOrSameValidator(this.minBidSize));
        maxDesiredPowerControl?.setValidators(MaxOrSameValidator(this.maxBidSize));

        this.initSubscribe();
      });
    } else {
      this.form = this.service.createForm();

      this.initSubscribe();
    }
  }

  private getProduct(id: number): void {
    if (!id) {
      this.productData = undefined;
      return;
    }

    this.auctionService.getProduct(id).subscribe((response: ProductDTO) => {
      this.productData = response;
      this.cdr.markForCheck();
    });
  }

  save(): void {
    let method: Observable<void>;

    if (!this.form) {
      return;
    }

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.toastr.warning('invalidForm');
      return;
    }

    const { deliveryDate, ...formData } = this.form.getRawValue();

    const data = {
      ...this.data,
      ...formData,
    };

    if (this.mode === 'add') {
      method = this.auctionService.save(data);
    } else {
      method = this.auctionService.update(this.config.data.id, data);
    }

    method
      .pipe(
        catchError((response: HttpErrorResponse): any => {
          if (!(response.status === 400 && response.error?.errorKey)) {
            this.toastr.error(new ToastrMessage({ msg: `auctions.actions.${this.mode}.error` }));
            return;
          }
        })
      )
      .subscribe(() => {
        this.toastr.success(new ToastrMessage({ msg: `auctions.actions.${this.mode}.success` }));
        this.close(true);
      });
  }

  private initSubscribe(): void {
    this.subscribeProduct();
  }

  private subscribeProduct(): void {
    const minDesiredPowerControl = this.form?.get('minDesiredPower');
    const maxDesiredPowerControl = this.form?.get('maxDesiredPower');

    this.form
      ?.get('product')
      ?.valueChanges.pipe(takeUntil(this.destroy$))
      .subscribe((product: ProductDTO) => {
        this.getProduct(product?.id);

        if (!product) {
          minDesiredPowerControl?.disable();
          maxDesiredPowerControl?.disable();
        } else {
          minDesiredPowerControl?.setValidators(MinOrSameValidator(this.minBidSize));
          maxDesiredPowerControl?.setValidators(MaxOrSameValidator(this.maxBidSize));

          minDesiredPowerControl?.enable();
          maxDesiredPowerControl?.enable();
        }
      });
  }
}
