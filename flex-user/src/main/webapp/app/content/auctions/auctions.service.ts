import * as moment from 'moment';

import { Injectable } from '@angular/core';

@Injectable()
export class AuctionsService {
  formatDeliveryDate(dateFrom: string, dateTo: string): string {
    const momentDateTo = moment(dateTo);

    const date = moment(dateFrom).format('DD/MM/YYYY');
    const fromTime = moment(dateFrom).format('HH:mm');
    const toTimeMinute = momentDateTo.format('mm');
    const toTimeHour = momentDateTo.isAfter(moment(dateFrom).endOf('day')) ? 24 : momentDateTo.format('HH');

    return `${date} ${fromTime} - ${toTimeHour}:${toTimeMinute}`;
  }
}
