import { Component, OnInit } from '@angular/core';
import { UntypedFormBuilder, Validators } from '@angular/forms';
import { AppToastrService } from '@app/core';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { ActivationsSettlementsService } from '../activations-settlements.service';
import { requiredIfAnyFilled } from '@app/shared/validators';
import * as moment from 'moment';

@Component({
  selector: 'app-export',
  templateUrl: './export.component.html',
})
export class ActivationsSettlementsExportComponent implements OnInit {
  maxDateTo!: Date | undefined;
  minDateFrom!: Date | undefined;

  constructor(
    private fb: UntypedFormBuilder,
    public ref: DynamicDialogRef,
    private toastr: AppToastrService,
    public config: DynamicDialogConfig,
    private service: ActivationsSettlementsService
  ) {}

  offerCategoryValidation = (key: string) =>
    requiredIfAnyFilled(
      ['daBids', 'cmvcBids'],
      ['daBids', 'cmvcBids'].filter((controlKey: string) => controlKey !== key)
    );

  form = this.fb.group({
    acceptedDeliveryPeriodFrom: [null, [Validators.required]],
    acceptedDeliveryPeriodTo: [null, [Validators.required]],
    daBids: [true, [this.offerCategoryValidation('daBids')]],
    cmvcBids: [true, [this.offerCategoryValidation('cmvcBids')]],
  });

  ngOnInit(): void {}

  close(): void {
    this.ref.close();
  }

  updateMinDateFrom() {
    const dateTo = this.form.get('acceptedDeliveryPeriodTo')?.value;
    this.minDateFrom = dateTo ? moment(dateTo).subtract(7, 'd').toDate() : undefined;
  }

  updateMaxDateTo() {
    const dateFrom = this.form.get('acceptedDeliveryPeriodFrom')?.value;
    this.maxDateTo = dateFrom ? moment(dateFrom).add(7, 'd').toDate() : undefined;
  }

  export(): void {
    const data = this.form.getRawValue();
    const dateFrom = moment(data.acceptedDeliveryPeriodFrom);
    const dateTo = moment(data.acceptedDeliveryPeriodTo);

    const offerCategory = [];

    if (data.daBids) {
      offerCategory.push('DAY_AHEAD');
    }

    if (data.cmvcBids) {
      offerCategory.push('CMVC');
    }

    if (!offerCategory.length) {
      this.toastr.warning('activationsSettlements.actions.export.warningNoType');
      this.form.markAllAsTouched();
      return;
    }

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    let filters = {
      'offerCategory.in': offerCategory,
      'acceptedDeliveryPeriodFrom.greaterThanOrEqual': dateFrom.clone().startOf('day').utc().format(),
      'acceptedDeliveryPeriodTo.lessThanOrEqual': dateTo.clone().startOf('day').add(1, 'd').utc().format(),
    };

    this.service.exportData(filters);
    this.close();
  }
}
