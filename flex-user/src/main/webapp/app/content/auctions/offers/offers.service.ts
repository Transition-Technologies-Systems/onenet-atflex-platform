import { AuctionOfferDTO } from './offer';
import { AuctionOffersParameters } from './offers.store';
import { AuctionType } from '../enums';
import { HttpService } from '@app/core';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable()
export class AuctionOffersService extends HttpService {
  protected url = 'api/user/auctions/offers';

  getAuctionUrl(auctionType: AuctionType): string {
    switch (auctionType) {
      case AuctionType.CMVC:
        return 'api/user/auctions-cmvc/offers';
      default:
        return 'api/user/auctions-day-ahead/offers';
    }
  }

  getOffers(parameters: AuctionOffersParameters): Observable<AuctionOfferDTO[]> {
    const { auctionCmvcId, auctionDayAheadId, page, size, filters, ...params } = parameters;
    const auctionType = auctionCmvcId ? AuctionType.CMVC : AuctionType.DAY_AHEAD;

    return this.get(this.getAuctionUrl(auctionType), {
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
