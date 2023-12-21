import * as moment from 'moment';

import { AuctionCmvcType, AuctionStatus } from '../../enums';
import { UntypedFormBuilder, UntypedFormGroup, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';

import { AuctionCmvcDTO } from '../cm-vc';
import { HttpClient } from '@angular/common/http';
import { HttpService } from '@app/core';
import { Injectable } from '@angular/core';

@Injectable()
export class CmVcDialogService extends HttpService {
  constructor(httpClient: HttpClient, private fb: UntypedFormBuilder) {
    super(httpClient);
  }

  createForm(data: Partial<AuctionCmvcDTO> = {}): UntypedFormGroup {
    const isOpenOrClosedAuction = [AuctionStatus.OPEN, AuctionStatus.CLOSED].includes(data.status || AuctionStatus.NEW);
    const isEdit = !!data.id;

    const defautlDeliveryDate = moment().startOf('day').add(1, 'd').toDate();
    const defautlDeliveryDateTo = moment(defautlDeliveryDate).add(1, 'day').toDate();
    const deliveryDate = data.deliveryDateFrom ? moment(data.deliveryDateFrom).toDate() : defautlDeliveryDate;

    return this.fb.group(
      {
        id: [{ value: data.id, disabled: true }],
        name: [{ value: data.name, disabled: true }],
        product: [{ value: data.product, disabled: isEdit }, Validators.required],
        localization: [{ value: data.localization, disabled: isEdit }],
        deliveryDate: [{ value: deliveryDate, disabled: isOpenOrClosedAuction }, Validators.required],
        deliveryDateFrom: [
          { value: this.formatToDate(data.deliveryDateFrom, defautlDeliveryDate), disabled: isOpenOrClosedAuction },
          Validators.required,
        ],
        deliveryDateTo: [
          { value: this.formatToDate(data.deliveryDateTo, defautlDeliveryDateTo), disabled: isOpenOrClosedAuction },
          Validators.required,
        ],
        gateOpeningTime: [{ value: this.formatToDate(data.gateOpeningTime), disabled: isOpenOrClosedAuction }, Validators.required],
        gateClosureTime: [{ value: this.formatToDate(data.gateClosureTime), disabled: isOpenOrClosedAuction }, Validators.required],
        minDesiredPower: [{ value: data.minDesiredPower, disabled: isOpenOrClosedAuction }],
        maxDesiredPower: [{ value: data.maxDesiredPower, disabled: isOpenOrClosedAuction }],
        auctionCmvcType: [{ value: data.auctionCmvcType || AuctionCmvcType.CAPACITY, disabled: true }],
      },
      { validators: [this.validateNoFilledDers()] }
    );
  }

  private formatToDate(value: string | undefined, defaultDate?: Date): Date | null {
    return value ? moment(value).toDate() : defaultDate || null;
  }

  private validateNoFilledDers(): ValidatorFn {
    return (form: any): ValidationErrors | null => {
      const { gateClosureTime, gateOpeningTime } = form.controls;
      const { sameDates, ...rest } = gateClosureTime.errors || {};
      if (moment(gateOpeningTime.value).isSame(gateClosureTime.value)) {
        gateClosureTime.setErrors({ ...rest, sameDates: true });
      } else {
        gateClosureTime.setErrors(Object.keys({ ...rest }).length ? { ...rest } : null);
      }
      return null;
    };
  }
}
