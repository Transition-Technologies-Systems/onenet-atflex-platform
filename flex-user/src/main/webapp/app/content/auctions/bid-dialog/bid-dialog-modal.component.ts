import { AuctionType } from '../enums';
import { Component, ViewChild } from '@angular/core';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';

import { AuctionBidComponent } from './bid-dialog.component';
import { AuctionOfferDTO } from '../offers/offer';
import { AuctionOfferTypeDTO } from './bid-dialog.service';
import { DialogExtends } from '@app/shared';

@Component({
  selector: 'app-auctions-bid-dialog',
  templateUrl: './bid-dialog-modal.component.html',
  styleUrls: ['./bid-dialog-modal.component.scss'],
})
export class AuctionBidModalComponent extends DialogExtends {
  @ViewChild(AuctionBidComponent) auctionBidComponent: AuctionBidComponent | undefined;

  titleParams: { auctionName: string; bidId?: number } = {
    auctionName: this.config.data.model?.name ?? this.config.data?.auctionName,
    bidId: this.config.data.bid?.id,
  };

  get auction(): AuctionOfferTypeDTO | undefined {
    return this.config.data?.model;
  }

  get auctionId(): number | undefined {
    return this.auction?.id || this.bid?.auctionId;
  }

  get auctionType(): AuctionType {
    return this.config.data?.type || AuctionType.CMVC;
  }

  get bid(): AuctionOfferDTO | undefined {
    return this.config.data?.bid;
  }

  get fromBids(): boolean {
    return !!this.config.data?.fromBids;
  }

  get saveDisabled(): boolean {
    return !!this.auctionBidComponent?.saveDisabled;
  }

  constructor(public ref: DynamicDialogRef, public config: DynamicDialogConfig) {
    super(ref, config);
  }

  save(): void {
    this.auctionBidComponent?.save();
  }
}
