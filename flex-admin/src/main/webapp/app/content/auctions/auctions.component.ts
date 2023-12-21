import { Component, ViewEncapsulation } from '@angular/core';

import { ActivatedRoute } from '@angular/router';
import { AuctionType } from './enums';
import { ConfirmationService } from 'primeng/api';

@Component({
  selector: 'app-auctions',
  templateUrl: './auctions.component.html',
  styleUrls: ['./auctions.component.scss'],
  encapsulation: ViewEncapsulation.None,
  providers: [ConfirmationService],
})
export class AuctionsComponent {
  type: AuctionType = AuctionType.CMVC;

  seperateFilterDates = [];
  setRangeFilterDateTime = ['createdDate', 'lastModifiedDate'];

  constructor(route: ActivatedRoute) {
    route.url.subscribe(() => {
      const data = route.snapshot.firstChild?.data || {};

      this.type = data.type === AuctionType.CMVC ? AuctionType.CMVC : AuctionType.DAY_AHEAD;
    });
  }
}
