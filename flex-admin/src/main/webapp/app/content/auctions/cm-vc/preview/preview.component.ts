import { AuctionBidComponent, AuctionBidModalComponent } from '../../bid-dialog';
import { ChangeDetectorRef, Component, OnInit, ViewChild } from '@angular/core';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';

import { AuctionCmvcDTO } from '../cm-vc';
import { AuctionOffersComponent } from '../../offers';
import { AuctionType } from '../../enums';
import { CmVcService } from '../cm-vc.service';
import { ModalService } from '@app/shared/commons';
import { ProductDTO } from '@app/shared/models';

@Component({
  selector: 'app-auctions-cmvc-preview',
  templateUrl: './preview.component.html',
})
export class CmVcPreviewComponent implements OnInit {
  @ViewChild(AuctionOffersComponent, { static: false }) offersComponent: AuctionOffersComponent | undefined;

  versionId: number = this.config?.data.id;
  type: AuctionType = this.config?.data?.type;
  data: AuctionCmvcDTO = this.config?.data?.model;
  productData: ProductDTO | undefined;
  auctionType = AuctionType.CMVC;

  constructor(
    public ref: DynamicDialogRef,
    private cdr: ChangeDetectorRef,
    public config: DynamicDialogConfig,
    private modalService: ModalService,
    private auctionService: CmVcService
  ) {}

  ngOnInit(): void {
    this.getData();
  }

  addBid(): void {
    this.modalService
      .open(AuctionBidModalComponent, { data: { model: this.data, type: AuctionType.CMVC }, styleClass: 'full-view' })
      .onClose.subscribe(() => this.offersComponent?.getCollection());
  }

  changeVersion(): void {
    this.getData();
  }

  close(): void {
    this.ref.close();
  }

  formatDeliveryDate(row: AuctionCmvcDTO): string {
    return this.auctionService.formatDeliveryDate(row);
  }

  formatLoalizations(row: AuctionCmvcDTO): string {
    return this.auctionService.formatLoalizations(row);
  }

  private getData(): void {
    this.auctionService.getAuction(this.data?.id).subscribe((response: AuctionCmvcDTO) => {
      this.data = response;

      this.auctionService.getProduct(this.data?.product?.id).subscribe((response: ProductDTO) => {
        this.productData = response;
        this.cdr.markForCheck();
      });

      this.cdr.markForCheck();
    });
  }
}
