import * as moment from 'moment';

import { Component, OnInit } from '@angular/core';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { UntypedFormBuilder, Validators } from '@angular/forms';

import { AppToastrService, ToastrMessage } from '@app/core';
import { BidsEvaluationService } from '../bids-evaluation.service';
import { requiredIfAnyFilled } from '@app/shared/validators';
import { AuctionsService } from '../../auctions.service';
import { AuctionEmailDTO } from '../../enums/auction-email-category';
import { TranslateService } from '@ngx-translate/core';
import { tap } from 'rxjs';

@Component({
  selector: 'app-auctions-bids-export',
  templateUrl: './export.component.html',
  providers: [],
})
export class BidsEvaluationExportDialogComponent implements OnInit {
  get exportConfig(): Partial<{ isSETO: boolean }> {
    return this.config.data ?? {};
  }

  constructor(
    private fb: UntypedFormBuilder,
    public ref: DynamicDialogRef,
    private toastr: AppToastrService,
    public config: DynamicDialogConfig,
    private service: BidsEvaluationService,
    private auctionsService: AuctionsService,
    private translate: TranslateService
  ) {}

  offerCategoryValidation = (key: string) =>
    requiredIfAnyFilled(
      ['balancingCapacityBids', 'balancingEnergyBids', 'cmvcBids'],
      ['balancingCapacityBids', 'balancingEnergyBids', 'cmvcBids'].filter((controlKey: string) => controlKey !== key)
    );

  form = this.fb.group({
    deliveryDate: [moment().add(1, 'd').toDate(), Validators.required],
    balancingCapacityBids: [true, this.offerCategoryValidation('balancingCapacityBids')],
    balancingEnergyBids: [true, this.offerCategoryValidation('balancingEnergyBids')],
    cmvcBids: [true, this.offerCategoryValidation('cmvcBids')],
  });

  ngOnInit(): void {}

  close(): void {
    this.ref.close();
  }

  export(): void {
    const data = this.form.getRawValue();
    const deliveryDate = moment(data.deliveryDate);

    const offerTypeAndCategory = [];

    if (data.balancingCapacityBids) {
      offerTypeAndCategory.push('DAY_AHEAD_CAPACITY');
    }

    if (data.balancingEnergyBids) {
      offerTypeAndCategory.push('DAY_AHEAD_ENERGY');
    }

    if (data.cmvcBids) {
      offerTypeAndCategory.push('CMVC_CAPACITY');
    }

    if (!offerTypeAndCategory.length) {
      this.toastr.warning('auctions.actions.bidsEvaluation.export.warningNoType');
      this.form.markAllAsTouched();
      return;
    }

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    let filters = {
      'auctionCategoryAndType.in': offerTypeAndCategory,
      'deliveryPeriod.greaterThanOrEqual': deliveryDate.clone().startOf('day').utc().format(),
      'deliveryPeriod.lessThanOrEqual': deliveryDate.clone().add(1, 'd').startOf('day').utc().format(),
    };

    const { emailCategory, isSendEmail } = this.config.data;

    if (isSendEmail) {
      this.auctionsService
        .sendPositionViaEmail(emailCategory, null, filters)
        .pipe(
          tap((response: AuctionEmailDTO) => {
            this.toastr.success(
              new ToastrMessage({
                msg: this.translate.instant('auctions.actions.sendViaEmail.success', { email: response.notifiedEmailAdress }),
              })
            );
          })
        )
        .subscribe();
    } else {
      this.service.exportXLSX(filters, this.exportConfig.isSETO as boolean);
    }

    this.close();
  }
}
