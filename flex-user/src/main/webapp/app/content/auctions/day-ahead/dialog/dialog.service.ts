import * as moment from 'moment';

import { AuctionsSeriesDTO } from '../day-ahead';
import { AuctionDayAheadType } from '../../enums';
import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';

import { HttpClient } from '@angular/common/http';
import { HttpService } from '@app/core';
import { Injectable } from '@angular/core';

@Injectable()
export class DayAheadDialogService extends HttpService {
  constructor(httpClient: HttpClient, private fb: UntypedFormBuilder) {
    super(httpClient);
  }

  createForm(data: Partial<AuctionsSeriesDTO> = {}): UntypedFormGroup {
    const isEdit = !!data.id;
    const type = data?.type || AuctionDayAheadType.ENERGY;
    const hasEnergy = [AuctionDayAheadType.ENERGY].includes(type);
    const hasCapacity = [AuctionDayAheadType.CAPACITY].includes(type);

    const defaultFirstAuctionDate = moment().startOf('day').add(1, 'd').toDate();
    const defaultLastAuctionDate = moment().startOf('day').add(1, 'd').toDate();
    const defaultGateOpeningTime = defaultFirstAuctionDate;
    const defaultGateClosureTime = moment(defaultFirstAuctionDate).add(1, 'd').toDate();

    const firstAuctionDate = this.formatToDate(data.firstAuctionDate, defaultFirstAuctionDate);
    const disableFirstAuctionDate = moment(firstAuctionDate).startOf('day').isSameOrBefore(moment());

    const lastAuctionDate = this.formatToDate(data.lastAuctionDate, defaultLastAuctionDate);
    const disableLastAuctionDate = moment(lastAuctionDate).startOf('day').isSameOrBefore(moment());

    return this.fb.group({
      id: [{ value: data.id, disabled: true }],
      name: [{ value: data.name, disabled: true }],
      type: [{ value: type, disabled: isEdit }, Validators.required],
      product: [{ value: data.product, disabled: isEdit }, Validators.required],

      energyGateOpeningTime: [
        { value: hasEnergy ? this.formatToDate(data.energyGateOpeningTime, defaultGateOpeningTime) : null, disabled: !hasEnergy },
        hasEnergy ? Validators.required : null,
      ],
      energyGateClosureTime: [
        { value: hasEnergy ? this.formatToDate(data.energyGateClosureTime, defaultGateClosureTime) : null, disabled: !hasEnergy },
        hasEnergy ? Validators.required : null,
      ],
      capacityGateOpeningTime: [
        { value: hasCapacity ? this.formatToDate(data.capacityGateOpeningTime, defaultGateOpeningTime) : null, disabled: !hasCapacity },
        hasCapacity ? Validators.required : null,
      ],
      capacityGateClosureTime: [
        { value: hasCapacity ? this.formatToDate(data.capacityGateClosureTime, defaultGateClosureTime) : null, disabled: !hasCapacity },
        hasCapacity ? Validators.required : null,
      ],

      maxDesiredCapacity: [{ value: data.maxDesiredCapacity, disabled: !hasCapacity }],
      maxDesiredEnergy: [{ value: data.maxDesiredEnergy, disabled: !hasEnergy }],
      minDesiredCapacity: [{ value: data.minDesiredCapacity, disabled: !hasCapacity }],
      minDesiredEnergy: [{ value: data.minDesiredEnergy, disabled: !hasEnergy }],

      capacityAvailabilityFrom: [
        {
          value: hasCapacity ? this.formatToDate(data.capacityAvailabilityFrom, defaultGateOpeningTime, firstAuctionDate) : null,
          disabled: !hasCapacity,
        },
        hasCapacity ? Validators.required : null,
      ],
      capacityAvailabilityTo: [
        {
          value: hasCapacity
            ? this.formatToDate(data.capacityAvailabilityTo, defaultGateClosureTime, firstAuctionDate, data.capacityAvailabilityFrom)
            : null,
          disabled: !hasCapacity,
        },
        hasCapacity ? Validators.required : null,
      ],
      energyAvailabilityFrom: [
        {
          value: hasEnergy ? this.formatToDate(data.energyAvailabilityFrom, defaultGateOpeningTime, firstAuctionDate) : null,
          disabled: !hasEnergy,
        },
        hasEnergy ? Validators.required : null,
      ],
      energyAvailabilityTo: [
        {
          value: hasEnergy
            ? this.formatToDate(data.energyAvailabilityFrom, defaultGateClosureTime, firstAuctionDate, data.energyAvailabilityFrom)
            : null,
          disabled: !hasEnergy,
        },
        hasEnergy ? Validators.required : null,
      ],

      firstAuctionDate: [{ value: firstAuctionDate, disabled: disableFirstAuctionDate }, Validators.required],
      lastAuctionDate: [{ value: lastAuctionDate, disabled: disableLastAuctionDate }, Validators.required],
    });
  }

  private formatToDate(value: string | undefined, defaultDate?: Date, correctToDay?: Date | null, fromDate?: string): Date | null {
    if (correctToDay) {
      const addDay = fromDate ? moment(fromDate).endOf('day').isBefore(moment(value).startOf('day')) : false;

      return value
        ? moment(value)
            .set({ date: moment(correctToDay).date() })
            .add(addDay ? 1 : 0, 'd')
            .toDate()
        : defaultDate || null;
    }

    return value ? moment(value).toDate() : defaultDate || null;
  }
}
