import { AppToastrService, ToastrMessage } from '@app/core';
import { AuctionsSeriesDTO } from '../day-ahead';
import { AuctionDayAheadType } from '../../enums';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { Dictionary, ProductDTO } from '@app/shared/models';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { Observable, of, takeUntil, catchError } from 'rxjs';
import { MaxOrSameValidator, MinOrSameValidator } from '@app/shared/commons/validators';
import { UntypedFormGroup, Validators } from '@angular/forms';

import { DayAheadDialogService } from './dialog.service';
import { DayAheadService } from '../day-ahead.service';
import { DialogExtends } from '@app/shared';
import { Helpers } from '@app/shared/commons';
import { HttpErrorResponse } from '@angular/common/http';
import { ProductDirection } from '@app/shared/enums';
import { moment } from 'polyfills';

interface Dictionaries {
  products$: Observable<Array<Dictionary & { minBidSize: number; maxBidSize: number }>>;
  types: Dictionary[];
}

@Component({
  selector: 'app-auctions-day-ahead-dialog',
  templateUrl: './dialog.component.html',
  providers: [DayAheadDialogService],
})
export class DayAheadDialogComponent extends DialogExtends implements OnInit {
  minDate = moment().startOf('day').add(1, 'd').toDate();
  currentDate = moment().startOf('day').toDate();

  form: UntypedFormGroup | undefined;
  productData: ProductDTO | undefined;
  data: AuctionsSeriesDTO = this.config.data;

  dictionaries: Dictionaries = {
    products$: of([]),
    types: Helpers.enumToDictionary(AuctionDayAheadType, 'AuctionDayAheadType'),
  };

  get capacityMaxDate(): Date {
    const capacityFrom = this.form?.get('capacityGateOpeningTime')?.value;
    const energyTo = this.form?.get('energyGateClosureTime')?.value;

    const maxDate = moment(capacityFrom).endOf('day').set({ m: 45, s: 0, ms: 0 });

    if (!!energyTo) {
      if (maxDate.isBefore(moment(energyTo))) {
        return maxDate.toDate();
      }

      return energyTo;
    }

    return maxDate.toDate();
  }

  get hasCapacity(): boolean {
    const type = this.form?.get('type')?.value as AuctionDayAheadType;

    return [AuctionDayAheadType.CAPACITY].includes(type);
  }

  get hasEnergy(): boolean {
    const type = this.form?.get('type')?.value as AuctionDayAheadType;

    return [AuctionDayAheadType.ENERGY].includes(type);
  }

  get minBidSize(): number {
    return this.form?.get('product')?.value?.minBidSize as number;
  }

  get maxBidSize(): number {
    return this.form?.get('product')?.value?.maxBidSize as number;
  }

  get maxDesiredCapacity(): number {
    const controlValue = this.form?.get('maxDesiredCapacity')?.value;

    if (controlValue || controlValue === 0) {
      return controlValue;
    }

    if (!this.maxBidSize && this.maxBidSize !== 0) {
      return 9999.99;
    }

    return this.maxBidSize;
  }

  get maxDesiredEnergy(): number {
    const controlValue = this.form?.get('maxDesiredEnergy')?.value;

    if (controlValue || controlValue === 0) {
      return controlValue;
    }

    if (!this.maxBidSize && this.maxBidSize !== 0) {
      return 9999.99;
    }

    return this.maxBidSize;
  }

  get minDesiredCapacity(): number {
    const controlValue = this.form?.get('minDesiredCapacity')?.value;

    if (controlValue || controlValue === 0) {
      return controlValue;
    }

    return this.minBidSize;
  }

  get minDesiredEnergy(): number {
    const controlValue = this.form?.get('minDesiredEnergy')?.value;

    if (controlValue || controlValue === 0) {
      return controlValue;
    }

    return this.minBidSize;
  }

  get minLastAuctionDate(): Date {
    const firstAuctionDate = this.form?.get('firstAuctionDate')?.value;

    if (!firstAuctionDate) {
      return this.minDate;
    }

    if (moment(firstAuctionDate).isAfter(moment(this.currentDate))) {
      return firstAuctionDate;
    }

    return this.currentDate;
  }

  get dateRangeMin(): Date {
    const min = this.form?.get('firstAuctionDate')?.value;

    return moment(min).startOf('day').set({ h: 1, m: 15 }).toDate();
  }

  constructor(
    private cdr: ChangeDetectorRef,
    public ref: DynamicDialogRef,
    public toastr: AppToastrService,
    public config: DynamicDialogConfig,
    private service: DayAheadDialogService,
    private auctionService: DayAheadService
  ) {
    super(ref, config);

    this.mode = this.config.data?.id ? 'edit' : 'add';
  }

  ngOnInit(): void {
    if (this.mode === 'edit') {
      this.auctionService.getSeries(this.config.data.id).subscribe((response: AuctionsSeriesDTO) => {
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
        if (this.mode === 'edit' && moment(data.lastAuctionDate).startOf('day').isSame(moment().startOf('date'))) {
          this.toastr.success(new ToastrMessage({ msg: `auctions.actions.edit.successWithEnd` }));
        } else {
          this.toastr.success(new ToastrMessage({ msg: `auctions.actions.${this.mode}.success` }));
        }

        this.close(true);
      });
  }

  private getProducts(): void {
    const type = this.form?.get('type')?.value;

    if (!type) {
      this.dictionaries.products$ = of([]);
      return;
    }

    const productDirection =
      type === AuctionDayAheadType.ENERGY ? [ProductDirection.UNDEFINED] : [ProductDirection.UP, ProductDirection.DOWN];

    this.dictionaries.products$ = this.auctionService.getProducts(productDirection);
  }

  private initSubscribe(): void {
    this.getProducts();

    this.subscribeProduct();
    this.subscribeType();
    this.subscribeFirstAuctionDate();
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

  private subscribeFirstAuctionDate(): void {
    this.form
      ?.get('firstAuctionDate')
      ?.valueChanges.pipe(takeUntil(this.destroy$))
      .subscribe(value => {
        const control = this.form?.get('lastAuctionDate');

        control?.setValue(value);
      });
  }

  private subscribeType(): void {
    const defaultFirstAuctionDate = moment().startOf('day').add(1, 'd').toDate();

    const capacityRequired = ['capacityGateOpeningTime', 'capacityGateClosureTime', 'capacityAvailabilityFrom', 'capacityAvailabilityTo'];
    const energyRequired = ['energyGateOpeningTime', 'energyGateClosureTime', 'energyAvailabilityFrom', 'energyAvailabilityTo'];

    const capacityFields = ['maxDesiredCapacity', 'minDesiredCapacity'];
    const energyFields = ['maxDesiredEnergy', 'minDesiredEnergy'];

    this.form
      ?.get('type')
      ?.valueChanges.pipe(takeUntil(this.destroy$))
      .subscribe((type: AuctionDayAheadType) => {
        if (!this.form) {
          return;
        }

        this.getProducts();

        const firstAuctionDateValue = this.form.get('firstAuctionDate')?.value ?? defaultFirstAuctionDate;
        const firstAuctionDate = moment(firstAuctionDateValue).set({ h: 1, m: 15 }).toDate();
        const defaultCapacityGateClosureTime = moment(firstAuctionDateValue).add(1, 'd').endOf('day').set({ m: 45, s: 0, ms: 0 }).toDate();
        const defaultEnergyGateClosureTime = moment(firstAuctionDateValue).add(1, 'd').toDate();
        const defaultGateOpeningTime = firstAuctionDate;

        const availabilityFrom = moment(firstAuctionDateValue).startOf('day').toDate();
        const availabilityTo = moment(availabilityFrom).add(1, 'd').toDate();

        if (type === AuctionDayAheadType.ENERGY) {
          Helpers.changeFormControlsState(this.form, [...capacityFields, ...capacityRequired], 'DISABLE');
          Helpers.changeFormControlsState(this.form, [...energyFields, ...energyRequired], 'ENABLE');
          Helpers.setValidatorToControls(this.form, energyRequired, [Validators.required]);

          this.form.patchValue(
            {
              energyGateOpeningTime: defaultCapacityGateClosureTime,
              energyGateClosureTime: defaultEnergyGateClosureTime,
              energyAvailabilityFrom: availabilityFrom,
              energyAvailabilityTo: availabilityTo,
            },
            { onlySelf: true, emitEvent: true }
          );
        } else {
          Helpers.changeFormControlsState(this.form, [...energyFields, ...energyRequired], 'DISABLE');
          Helpers.changeFormControlsState(this.form, [...capacityFields, ...capacityRequired], 'ENABLE');
          Helpers.setValidatorToControls(this.form, capacityRequired, [Validators.required]);

          this.form.patchValue(
            {
              capacityGateOpeningTime: defaultGateOpeningTime,
              capacityGateClosureTime: defaultCapacityGateClosureTime,
              capacityAvailabilityFrom: availabilityFrom,
              capacityAvailabilityTo: availabilityTo,
            },
            { onlySelf: true, emitEvent: true }
          );
        }
      });
  }
}
