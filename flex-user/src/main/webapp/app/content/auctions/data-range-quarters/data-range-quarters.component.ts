import * as moment from 'moment';

import { ChangeDetectionStrategy, Component, Input, OnChanges, OnDestroy, SimpleChanges } from '@angular/core';
import { Subject, takeUntil } from 'rxjs';

import { UntypedFormGroup } from '@angular/forms';

@Component({
  selector: 'app-auctions-data-range-quarters',
  templateUrl: './data-range-quarters.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DataRangeQuartersComponent implements OnDestroy, OnChanges {
  @Input() form: UntypedFormGroup | undefined;
  @Input() disabled = false;

  @Input() prefix = 'auctions.table';
  @Input() showDateControl = true;
  @Input() dateControlName = 'deliveryDate';
  @Input() dateToControlName = 'deliveryDateTo';
  @Input() dateFromControlName = 'deliveryDateFrom';

  @Input() dateDiabled = false;
  @Input() min: Date | undefined;
  @Input() max: Date | undefined;

  minDeliveryDate = moment().startOf('day').add(1, 'd').toDate();
  deliveryDateFromMin = this.getDeliveryDateFromMin();
  deliveryDateFromMax = this.getDeliveryDateFromMax();
  deliveryDateToMax = this.getDeliveryDateToMax();

  get deliveryDateLabel(): string {
    return `${this.prefix}.${this.dateControlName}`;
  }

  get deliveryDateFromLabel(): string {
    return `${this.prefix}.${this.dateFromControlName}`;
  }

  get deliveryDateToLabel(): string {
    return `${this.prefix}.${this.dateToControlName}`;
  }

  private initialized = false;
  private destroy$ = new Subject<void>();

  constructor() {}

  ngOnChanges(changes: SimpleChanges): void {
    this.deliveryDateFromMin = this.getDeliveryDateFromMin();
    this.deliveryDateFromMax = this.getDeliveryDateFromMax();
    this.deliveryDateToMax = this.getDeliveryDateToMax();

    if (changes.form && this.form && !this.initialized) {
      this.subscribeDate();
      this.subscribeDateFrom();
      this.subscribeDateTo();

      this.initialized = true;
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  getDeliveryDateFromMin(): Date {
    const fromDate = this.form?.get(this.dateFromControlName)?.value;

    if (!fromDate) {
      return this.min ?? moment(this.minDeliveryDate).add(15, 'm').set({ s: 0, ms: 0 }).toDate();
    }

    const minDate = moment(fromDate).add(15, 'm').set({ s: 0, ms: 0 }).toDate();

    if (this.min && moment(this.min).isAfter(moment(minDate))) {
      return this.min;
    }

    return minDate;
  }

  getDeliveryDateFromMax(): Date {
    const toDate = this.form?.get(this.dateToControlName)?.value;
    const deliveryDate = this.form?.get(this.dateControlName)?.value;
    const maxDeliveryFromDate =
      this.max ??
      moment(deliveryDate ?? this.minDeliveryDate)
        .endOf('day')
        .set('m', 45)
        .set({ s: 0, ms: 0 })
        .toDate();

    if (!toDate) {
      return maxDeliveryFromDate;
    }

    if (moment(toDate).isAfter(moment(maxDeliveryFromDate))) {
      return maxDeliveryFromDate;
    }

    return moment(toDate).subtract(15, 'm').toDate();
  }

  getDeliveryDateToMax(): Date {
    if (this.max) {
      return this.max;
    }

    const deliveryDate = this.form?.get(this.dateControlName)?.value;

    return moment(deliveryDate).startOf('day').add(1, 'd').set({ s: 0, ms: 0 }).toDate();
  }

  private subscribeDate(): void {
    const dateFromControl = this.form?.get(this.dateFromControlName);
    const dateToControl = this.form?.get(this.dateToControlName);

    this.form
      ?.get(this.dateControlName)
      ?.valueChanges.pipe(takeUntil(this.destroy$))
      .subscribe(date => {
        let hourFrom = 0;
        let minuteFrom = 0;
        let hourTo = 24;
        let minuteTo = 0;

        if (!date) {
          dateFromControl?.disable();
          dateToControl?.disable();

          return;
        } else {
          dateFromControl?.enable();
          dateToControl?.enable();
        }

        if (dateFromControl?.value) {
          hourFrom = moment(dateFromControl.value).hour();
          minuteFrom = moment(dateFromControl.value).minute();
        }

        if (dateToControl?.value) {
          let checkDate = true;

          if (dateFromControl?.value) {
            if (moment(dateFromControl.value).endOf('day').isBefore(moment(dateToControl.value))) {
              checkDate = false;
              hourTo = 24;
            }
          }

          if (checkDate) {
            hourTo = moment(dateToControl.value).hour();
            minuteTo = moment(dateToControl.value).minute();
          }
        }

        this.form?.patchValue({
          [this.dateFromControlName]: moment(date).add(hourFrom, 'h').add(minuteFrom, 'm').toDate(),
          [this.dateToControlName]: moment(date).add(hourTo, 'h').add(minuteTo, 'm').toDate(),
        });

        this.deliveryDateFromMin = this.getDeliveryDateFromMin();
        this.deliveryDateFromMax = this.getDeliveryDateFromMax();
        this.deliveryDateToMax = this.getDeliveryDateToMax();
      });
  }

  private subscribeDateFrom(): void {
    this.form
      ?.get(this.dateFromControlName)
      ?.valueChanges.pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        this.deliveryDateFromMin = this.getDeliveryDateFromMin();
      });
  }

  private subscribeDateTo(): void {
    this.form
      ?.get(this.dateToControlName)
      ?.valueChanges.pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        this.deliveryDateFromMax = this.getDeliveryDateFromMax();
      });
  }
}
