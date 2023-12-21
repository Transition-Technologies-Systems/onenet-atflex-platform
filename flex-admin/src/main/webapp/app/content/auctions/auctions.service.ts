import * as moment from 'moment';

import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpService } from '@app/core';
import { AuctionEmailCategory, AuctionEmailDTO } from './enums/auction-email-category';
import { HttpClient } from '@angular/common/http';

@Injectable()
export class AuctionsService extends HttpService {
  constructor(httpClient: HttpClient) {
    super(httpClient);
  }

  formatDeliveryDate(dateFrom: string, dateTo: string): string {
    const momentDateTo = moment(dateTo);

    const date = moment(dateFrom).format('DD/MM/YYYY');
    const fromTime = moment(dateFrom).format('HH:mm');
    const toTimeMinute = momentDateTo.format('mm');
    const toTimeHour = momentDateTo.isAfter(moment(dateFrom).endOf('day')) ? 24 : momentDateTo.format('HH');

    return `${date} ${fromTime} - ${toTimeHour}:${toTimeMinute}`;
  }

  sendPositionViaEmail(emailCategory: string, id?: number | null, filters?: any): Observable<AuctionEmailDTO> {
    const { BID, ALGORITHM_RESULT, TSO_EXPORT, DSO_SETO_EXPORT } = AuctionEmailCategory;
    let url = '';
    if (id) {
      switch (emailCategory) {
        case BID:
          url = `flex-server/api/admin/algorithm/${id}/offers/export/email`;
          break;
        case ALGORITHM_RESULT:
          url = `flex-server/api/admin/algorithm/evaluation/${id}/results/email`;
          break;
      }
    } else {
      switch (emailCategory) {
        case TSO_EXPORT:
          url = `flex-server/api/admin/auctions/offers/view/export/email`;
          break;
        case DSO_SETO_EXPORT:
          url = `flex-server/api/admin/auctions/offers/view/export/email/seto`;
          break;
      }
    }
    return this.get<AuctionEmailDTO>(url, {
      params: filters,
    });
  }
}
