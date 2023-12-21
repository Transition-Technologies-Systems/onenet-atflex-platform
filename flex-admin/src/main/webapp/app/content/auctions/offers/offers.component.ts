import { ActivatedRoute, Router } from '@angular/router';
import { AfterViewInit, Component, ElementRef, Injector, Input, OnChanges, OnInit } from '@angular/core';
import { AppToastrService, SessionStorageService } from '@app/core';
import { AuctionOfferStatus, AuctionDayAheadType, AuctionStatus, AuctionType } from '../enums';
import { AuctionOffersParameters, AuctionOffersStore } from './offers.store';
import { Helpers, ModalService } from '@app/shared/commons';
import { Subscription, first, takeUntil, catchError } from 'rxjs';

import { AuctionBidModalComponent } from '../bid-dialog';
import { AuctionCmvcDTO } from '../cm-vc/cm-vc';
import { AuctionDayAheadDTO } from '../day-ahead/day-ahead';
import { AuctionOfferDTO } from './offer';
import { AuctionOffersService } from './offers.service';
import { AuctionsService } from '../auctions.service';
import { COLUMNS } from './offers.columns';
import { ConfirmationService } from 'primeng/api';
import { Dictionary } from '@app/shared/models';
import { DynamicDialogConfig } from 'primeng/dynamicdialog';
import { HttpErrorResponse } from '@angular/common/http';
import { RxStompService } from '@stomp/ng2-stompjs';
import { TableExtends } from '@app/shared/services';
import { TranslateService } from '@ngx-translate/core';
import { ViewConfigurationService } from '@app/shared/commons/view-configuration';

interface Dictionaries {
  statuses: Dictionary[];
}

@Component({
  selector: 'app-auctions-offers',
  templateUrl: './offers.component.html',
  styleUrls: ['./offers.component.scss'],
  providers: [AuctionOffersStore, ConfirmationService],
})
export class AuctionOffersComponent extends TableExtends implements OnInit, OnChanges, AfterViewInit {
  @Input() evaluationId: number | undefined;
  @Input() type: 'preview' | 'edit' = 'edit';
  @Input() auctionCmvcId: number | undefined;
  @Input() auctionDayAheadId: number | undefined;
  @Input() auctionType: AuctionType = AuctionType.CMVC;
  @Input() auctionDayAheadType: 'CAPACITY' | 'ENERGY' = 'CAPACITY';
  @Input() auction: AuctionCmvcDTO | AuctionDayAheadDTO | undefined;

  viewName = 'auction-offers';
  inModal = true;
  totalRecords = 0;
  offersHeight = 0;
  virtualScrollOn = false;
  data$ = this.store.data$;
  dataIds$ = this.store.dataIds$;
  parameters: AuctionOffersParameters | undefined;
  columns = this.preparedColumns(COLUMNS, 'auctions.offers');

  dictionaries: Dictionaries = {
    statuses: Helpers.enumToDictionary(AuctionOfferStatus, 'AuctionOfferStatus'),
  };

  get authoritiesDelete(): string {
    switch (this.auctionType) {
      case AuctionType.CMVC:
        return 'FLEX_ADMIN_AUCTIONS_CMVC_OFFER_DELETE';
      case AuctionType.DAY_AHEAD:
        return 'FLEX_ADMIN_AUCTIONS_DAY_AHEAD_OFFER_DELETE';
    }
    return '';
  }

  get authoritiesEdit(): string {
    switch (this.auctionType) {
      case AuctionType.CMVC:
        return 'FLEX_ADMIN_AUCTIONS_CMVC_OFFER_EDIT';
      case AuctionType.DAY_AHEAD:
        return 'FLEX_ADMIN_AUCTIONS_DAY_AHEAD_OFFER_EDIT';
    }
    return '';
  }

  get authoritiesView(): string {
    switch (this.auctionType) {
      case AuctionType.CMVC:
        return 'FLEX_ADMIN_AUCTIONS_CMVC_OFFER_VIEW';
      case AuctionType.DAY_AHEAD:
        return 'FLEX_ADMIN_AUCTIONS_DAY_AHEAD_OFFER_VIEW';
    }
    return '';
  }

  get editEnabled(): boolean {
    if (!this.auction) {
      return false;
    }

    if (this.auctionType === AuctionType.CMVC) {
      return [AuctionStatus.OPEN, AuctionStatus.CLOSED].includes(this.auction.status);
    }

    return [AuctionStatus.OPEN_CAPACITY, AuctionStatus.CLOSED_CAPACITY, AuctionStatus.OPEN_ENERGY, AuctionStatus.CLOSED_ENERGY].includes(
      this.auction.status
    );
  }

  get deleteEnabled(): boolean {
    if (!this.auction) {
      return false;
    }

    if (this.auctionType === AuctionType.CMVC) {
      return [AuctionStatus.OPEN].includes(this.auction.status);
    }

    return [AuctionStatus.OPEN_CAPACITY, AuctionStatus.OPEN_ENERGY].includes(this.auction.status);
  }

  get isDa(): boolean {
    return this.auctionType === AuctionType.DAY_AHEAD;
  }

  get offersTableScrollHeight(): string {
    const height = Math.min(this.offersHeight + 19 + 113, this.wrapperHeight);

    return `${height}px`;
  }

  get rowHeight(): number {
    return this.editEnabled || this.deleteEnabled ? 45 : 37;
  }

  private subscribeEnabled = false;
  private websocketSub: Subscription | undefined;

  constructor(
    injector: Injector,
    route: ActivatedRoute,
    elementRef: ElementRef,
    public router: Router,
    private toastr: AppToastrService,
    private store: AuctionOffersStore,
    sessionStorage: SessionStorageService,
    public config: DynamicDialogConfig,
    private modalService: ModalService,
    private translate: TranslateService,
    private service: AuctionOffersService,
    private rxStompService: RxStompService,
    private auctionService: AuctionsService,
    private confirmationService: ConfirmationService,
    protected viewConfigurationService: ViewConfigurationService
  ) {
    super(injector, route, elementRef, sessionStorage, viewConfigurationService);
  }

  ngOnInit(): void {
    this.getCollection();
    this.subscribeDynamicFilters();
    this.subscribeTotalRecords();
  }

  ngOnChanges(): void {
    this.columns = this.preparedColumns(COLUMNS, 'auctions.offers').filter(({ field }) => {
      if (this.evaluationId) {
        return !['actions'].includes(field);
      }

      if (this.auctionType === AuctionType.CMVC) {
        return !['schedulingUnit', 'priceKwh', 'potentialFromSU'].includes(field);
      }

      let fields = ['flexPotential', 'potentialFromFlex'];

      switch (this.auctionDayAheadType) {
        case 'CAPACITY':
          fields = [...fields, 'priceKwh'];
          break;
        case 'ENERGY':
          fields = [...fields, 'price'];
          break;
      }

      return !fields.includes(field);
    });

    this.onActiveColumnsChange(false);
  }

  ngAfterViewInit(): void {
    this.afterViewInit();

    this.subscribeAuction();
  }

  delete(event: Event, row: AuctionOfferDTO): void {
    this.confirmationService.confirm({
      target: event.target || undefined,
      message: this.translate.instant('auctions.actions.bids.delete.question'),
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.service
          .remove(row.id, this.auctionType)
          .pipe(
            catchError((response: HttpErrorResponse): any => {
              if (!(response.status === 400 && response.error?.errorKey)) {
                this.toastr.error('auctions.actions.delete.error');
                return;
              }
            })
          )
          .subscribe(() => {
            this.toastr.success('auctions.actions.bids.delete.success');
            this.getCollection();
          });
      },
    });
  }

  edit(row: AuctionOfferDTO): void {
    this.modalService
      .open(AuctionBidModalComponent, {
        data: { model: this.auction, bid: row, id: row.id, type: this.auctionType },
        styleClass: 'full-view',
      })
      .onClose.pipe(takeUntil(this.destroy$))
      .subscribe(() => this.getCollection());
  }

  formatAcceptedDeliveryDate(row: AuctionOfferDTO): string {
    return this.auctionService.formatDeliveryDate(row.acceptedDeliveryPeriodFrom, row.acceptedDeliveryPeriodTo);
  }

  formatDeliveryDate(row: AuctionOfferDTO): string {
    return this.auctionService.formatDeliveryDate(row.deliveryPeriodFrom, row.deliveryPeriodTo);
  }

  getCollection(): void {
    const dateTimeKeys: string[] = [];

    if (!this.auctionCmvcId && !this.auctionDayAheadId) {
      return;
    }

    const filters = Helpers.serializeFilters(this.staticFilters, this.dynamicFilters, dateTimeKeys);

    this.parameters = {
      page: this.page,
      size: this.rows,
      sort: this.sort,
      evaluationId: this.evaluationId,
      auctionCmvcId: this.auctionCmvcId,
      auctionDayAheadId: this.auctionDayAheadId,
      filters: {
        ...filters,
        'type.equals': this.auctionDayAheadType,
      },
    };

    this.sendFiltersToWebsocket();

    this.store.loadCollection({
      ...this.parameters,
      runAfterGetData: () => this.updateHandyScroll(),
    });
  }

  getRowClass(row: AuctionOfferDTO): string {
    if (row.status === AuctionOfferStatus.ACCEPTED) {
      return 'bid-green';
    } else if (row.status === AuctionOfferStatus.VOLUMES_VERIFIED) {
      return 'volumes-verified';
    } else if ([AuctionOfferStatus.REJECTED].includes(row.status)) {
      return 'bid-red';
    } else if ([AuctionOfferStatus.PENDING].includes(row.status)) {
      return 'bid-yellow';
    }

    return '';
  }

  getUnit(type: 'price' | 'volume', row: AuctionOfferDTO): string {
    switch (row.type) {
      case AuctionDayAheadType.CAPACITY:
        return type === 'price' ? 'PLN/kW' : 'kW';
      case AuctionDayAheadType.ENERGY:
        return type === 'price' ? 'PLN/kWh' : 'kWh';
    }
  }

  getValue(row: AuctionOfferDTO, key: 'price' | 'volume' | 'acceptedVolume', unit?: string): string | number {
    return unit ? `${row[key]} ${unit}` : row[key];
  }

  private sendFiltersToWebsocket(): void {
    if (!this.subscribeEnabled) {
      return;
    }

    this.rxStompService.publish({
      destination: '/app/refresh-view/offers',
      body: JSON.stringify({
        auctionCmvcId: this.parameters?.auctionCmvcId,
        auctionDayAheadId: this.parameters?.auctionDayAheadId,
        auctionType: this.parameters?.filters?.auctionDayAheadType,
        ...this.parameters?.filters,
      }),
    });
  }

  private subscribeAuction(): void {
    this.subscribeEnabled = true;
    this.watchRxStomp();
  }

  private subscribeTotalRecords(): void {
    this.store.totalRecords$.pipe(takeUntil(this.destroy$)).subscribe((records: number) => {
      this.totalRecords = records;
      this.offersHeight = records * this.rowHeight;

      this.virtualScrollOn = this.totalRecords > 20;

      this.table?.cd.detectChanges();
    });
  }

  private watchRxStomp(): void {
    if (!this.subscribeEnabled || this.evaluationId) {
      return;
    }

    this.rxStompService.connected$.pipe(first()).subscribe(connected => {
      if (connected === 1) {
        this.sendFiltersToWebsocket();
      }
    });

    this.websocketSub = this.rxStompService
      .watch(`/refresh-view/auctions/offer`)
      .pipe(takeUntil(this.destroy$))
      .subscribe(message => {
        const data: AuctionOfferDTO = JSON.parse(message.body || '');

        if (!data) {
          return;
        }

        this.store.upsertOne(data);
      });
  }
}
