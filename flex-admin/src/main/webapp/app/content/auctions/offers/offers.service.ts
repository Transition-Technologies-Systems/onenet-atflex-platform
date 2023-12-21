import { AuctionOfferDTO } from './offer';
import { AuctionOffersParameters } from './offers.store';
import { AuctionType } from '../enums';
import { HttpService } from '@app/core';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable()
export class AuctionOffersService extends HttpService {
  protected url = 'flex-server/api/admin/auctions/offers';

  getAuctionUrl(auctionType: AuctionType): string {
    switch (auctionType) {
      case AuctionType.CMVC:
        return 'flex-server/api/admin/auctions-cmvc/offers';
      default:
        return 'flex-server/api/admin/auctions-day-ahead/offers';
    }
  }

  getOffers(parameters: AuctionOffersParameters): Observable<AuctionOfferDTO[]> {
    const { auctionCmvcId, auctionDayAheadId, evaluationId, page, size, filters, ...params } = parameters;
    const auctionType = auctionCmvcId ? AuctionType.CMVC : AuctionType.DAY_AHEAD;

    const url = evaluationId ? `flex-server/api/admin/algorithm/${evaluationId}/offers` : this.getAuctionUrl(auctionType);

    return this.get(url, {
      params: {
        ...params,
        ...filters,
        'auctionCmvc.in': auctionCmvcId,
        'auctionDayAhead.in': auctionDayAheadId,
      },
    });
  }

  remove(id: number, auctionType: AuctionType): Observable<void> {
    return this.delete(`${this.getAuctionUrl(auctionType)}/${id}`);
  }
}
