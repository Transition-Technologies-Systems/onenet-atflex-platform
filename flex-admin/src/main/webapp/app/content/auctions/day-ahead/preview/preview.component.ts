import { AuctionDayAheadDTO, AuctionsSeriesDTO, TabType } from '../day-ahead';
import { AuctionDayAheadType, AuctionStatus, AuctionType } from '../../enums';
import { ChangeDetectorRef, Component, OnInit, QueryList, ViewChildren } from '@angular/core';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';

import { AuctionBidModalComponent } from '../../bid-dialog';
import { AuctionOffersComponent } from '../../offers';
import { DayAheadService } from '../day-ahead.service';
import { ModalService } from '@app/shared/commons';
import { ProductDTO } from '@app/shared/models';
import { Router } from '@angular/router';

@Component({
  selector: 'app-auctions-day-ahead-preview',
  templateUrl: './preview.component.html',
})
export class DayAheadPreviewComponent implements OnInit {
  @ViewChildren(AuctionOffersComponent) offersComponent: QueryList<AuctionOffersComponent> | undefined;

  data: AuctionDayAheadDTO | AuctionsSeriesDTO = this.config?.data?.model;
  isSeriesView = !!this.config.data?.isSeriesView;
  type: AuctionType = this.config.data?.type;
  versionId: number = this.config.data.id;
  tab: TabType = this.config.data.tab;
  productData: ProductDTO | undefined;
  auctionType = AuctionType.DAY_AHEAD;

  auctionDayAheadType: typeof AuctionDayAheadType = AuctionDayAheadType;

  get auction(): AuctionDayAheadDTO | undefined {
    return this.isSeriesView ? undefined : (this.data as AuctionDayAheadDTO);
  }

  get series(): AuctionsSeriesDTO | undefined {
    return this.isSeriesView ? (this.data as AuctionsSeriesDTO) : undefined;
  }

  get isCapacityTab(): boolean {
    return this.tab === 'capacity-auctions';
  }

  get isEnergyTab(): boolean {
    return this.tab === 'energy-auctions';
  }

  get isOpenAuction(): boolean {
    if (!this.auction) {
      return false;
    }

    return [AuctionStatus.OPEN_CAPACITY, AuctionStatus.OPEN_ENERGY, AuctionStatus.OPEN].includes(this.auction.status);
  }

  constructor(
    public router: Router,
    public ref: DynamicDialogRef,
    private cdr: ChangeDetectorRef,
    public config: DynamicDialogConfig,
    private modalService: ModalService,
    private auctionService: DayAheadService
  ) {}

  ngOnInit(): void {
    this.getData();
  }

  addBid(): void {
    this.modalService
      .open(AuctionBidModalComponent, { data: { model: this.data, type: AuctionType.DAY_AHEAD }, styleClass: 'full-view' })
      .onClose.subscribe(() => {
        if (!!this.offersComponent) {
          this.offersComponent.toArray().forEach((offersComponent: AuctionOffersComponent) => {
            offersComponent.getCollection();
          });
        }
      });
  }

  changeVersion(): void {
    this.getData();
  }

  close(): void {
    this.ref.close();
  }

  formatAvailability(row: AuctionDayAheadDTO | AuctionsSeriesDTO, type: 'capacity' | 'energy'): string {
    if (type === 'capacity') {
      return this.auctionService.formatAvailability(row.capacityAvailabilityFrom, row.capacityAvailabilityTo);
    }

    return this.auctionService.formatAvailability(row.energyAvailabilityFrom, row.energyAvailabilityTo);
  }

  private getData(): void {
    if (this.isSeriesView) {
      const data = this.data as AuctionsSeriesDTO;

      this.auctionService.getSeries(data?.id).subscribe((response: AuctionsSeriesDTO) => {
        this.data = response;

        this.auctionService.getProduct(response?.product?.id).subscribe((response: ProductDTO) => {
          this.productData = response;
          this.cdr.markForCheck();
        });

        this.cdr.markForCheck();
      });
    } else {
      const data = this.data as AuctionDayAheadDTO;

      this.auctionService.getAuction(data?.id).subscribe((response: AuctionDayAheadDTO) => {
        this.data = response;

        this.auctionService.getProduct(data?.productId).subscribe((response: ProductDTO) => {
          this.productData = response;
          this.cdr.markForCheck();
        });

        this.cdr.markForCheck();
      });
    }
  }
}
