import { ActivatedRoute, Router } from '@angular/router';
import { AfterViewInit, ChangeDetectorRef, Component, ElementRef, Injector } from '@angular/core';
import { AppToastrService, SessionStorageService } from '@app/core';
import { AuctionDayAheadDTO, AuctionsSeriesDTO, Tab, TabType } from './day-ahead';
import { AuctionDayAheadType, AuctionStatus, AuctionType } from '../enums';
import { DayAheadParameters, DayAheadStore } from './day-ahead.store';
import { Helpers, ModalService } from '@app/shared/commons';

import { AuctionBidModalComponent } from '../bid-dialog';
import { COLUMNS } from './day-ahead.columns';
import { ConfirmationService } from 'primeng/api';
import { DayAheadDialogComponent } from './dialog';
import { DayAheadPreviewComponent } from './preview';
import { DayAheadService } from '.';
import { Dictionary } from '@app/shared/models';
import { HttpErrorResponse } from '@angular/common/http';
import { RxStompService } from '@stomp/ng2-stompjs';
import { TableExtends } from '@app/shared/services';
import { TranslateService } from '@ngx-translate/core';
import { ViewConfigurationService } from '@app/shared/commons/view-configuration';
import { takeUntil, catchError } from 'rxjs';

interface Dictionaries {
  statuses: Dictionary[];
  types: Dictionary[];
}

@Component({
  selector: 'app-auctions-day-ahead',
  templateUrl: './day-ahead.component.html',
  providers: [DayAheadStore],
})
export class DayAheadComponent extends TableExtends implements AfterViewInit {
  selectedTab: TabType = 'capacity-auctions';
  sort = 'statusCode,capacityGateClosureTime,asc';

  data$ = this.store.data$;
  dataIds$ = this.store.dataIds$;
  totalRecords$ = this.store.totalRecords$;
  parameters: DayAheadParameters | undefined;
  filtersWithDateToNextDay = ['deliveryDate'];
  columns = this.preparedColumns(COLUMNS, 'auctions.table');
  auctionStatus: typeof AuctionStatus = AuctionStatus;

  seperateFilterDates = [];
  setRangeFilterDateTime = ['createdDate', 'lastModifiedDate', 'gateDate'];
  filtersWithTime = ['gateDate', 'capacityGateOpeningTime', 'capacityGateClosureTime', 'energyGateOpeningTime', 'energyGateClosureTime'];

  dictionaries: Dictionaries = {
    statuses: this.service.getStatusDictionary(this.selectedTab),
    types: Helpers.enumToDictionary(AuctionDayAheadType, 'AuctionDayAheadType'),
  };

  get isCapacityView(): boolean {
    return this.selectedTab === 'capacity-auctions';
  }

  get isSeriesView(): boolean {
    return this.selectedTab === 'series-auctions';
  }

  get viewName(): string {
    return `auctions-day-ahead-${this.selectedTab}`;
  }

  constructor(
    injector: Injector,
    route: ActivatedRoute,
    elementRef: ElementRef,
    private store: DayAheadStore,
    private service: DayAheadService,
    private toastr: AppToastrService,
    sessionStorage: SessionStorageService,
    private modalService: ModalService,
    private translate: TranslateService,
    private rxStompService: RxStompService,
    private confirmationService: ConfirmationService,
    protected viewConfigurationService: ViewConfigurationService
  ) {
    super(injector, route, elementRef, sessionStorage, viewConfigurationService);

    this.preparedColumnsForTab();
  }

  ngAfterViewInit(): void {
    this.afterViewInit();

    this.watchRxStomp();
  }

  add(): void {
    const dialog = this.modalService.open(DayAheadDialogComponent, { styleClass: 'full-view' });

    dialog.onClose.pipe(takeUntil(this.destroy$)).subscribe((result: boolean) => {
      if (!!result) {
        this.getCollection();
      }
    });
  }

  addBid(row: AuctionDayAheadDTO): void {
    this.service
      .getAuction(row.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe(resp => {
        this.modalService
          .open(AuctionBidModalComponent, { data: { model: resp, type: AuctionType.DAY_AHEAD }, styleClass: 'full-view' })
          .onClose.subscribe(() => {
            this.getCollection();
          });
      });
  }

  delete(event: Event, row: AuctionsSeriesDTO): void {
    this.confirmationService.confirm({
      target: event.target || undefined,
      message: this.translate.instant('auctions.actions.delete.question'),
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.service
          .remove(row.id)
          .pipe(
            catchError((response: HttpErrorResponse): any => {
              if (!(response.status === 400 && response.error?.errorKey)) {
                this.toastr.error('auctions.actions.delete.error');
                return;
              }
            })
          )
          .subscribe(() => {
            if (row.deletable) {
              this.toastr.success('auctions.actions.delete.success');
            } else {
              this.toastr.success('auctions.actions.delete.successWithEnd');
            }

            this.getCollection();
          });
      },
    });
  }

  edit(row: AuctionsSeriesDTO): void {
    const dialog = this.modalService.open(DayAheadDialogComponent, { data: row, styleClass: 'full-view' });

    dialog.onClose.pipe(takeUntil(this.destroy$)).subscribe((result: boolean) => {
      if (!!result) {
        this.getCollection();
      }
    });
  }

  formatAvailability(row: AuctionDayAheadDTO | AuctionsSeriesDTO, type: 'capacity' | 'energy'): string {
    if (type === 'capacity') {
      return this.service.formatAvailability(row.capacityAvailabilityFrom, row.capacityAvailabilityTo);
    }

    return this.service.formatAvailability(row.energyAvailabilityFrom, row.energyAvailabilityTo);
  }

  getCollection(): void {
    const dateTimeKeys = [
      'createdDate',
      'lastModifiedDate',
      'gateDate',
      'deliveryDate',
      'capacityGateOpeningTime',
      'capacityGateClosureTime',
      'energyGateOpeningTime',
      'energyGateClosureTime',
    ];

    this.parameters = {
      page: this.page,
      size: this.rows,
      sort: this.sort,
      tab: this.selectedTab,
      filters: Helpers.serializeFilters(
        {
          ...this.staticFilters,
          'auctionType.equals': this.service.tabTypeToType(this.selectedTab),
        },
        this.dynamicFilters,
        dateTimeKeys
      ),
    };

    this.store.loadCollection({
      ...this.parameters,
      runAfterGetData: () => this.updateHandyScroll(),
    });
  }

  getUnit(type: 'price' | 'volume', row: AuctionDayAheadDTO): string {
    switch (row.type) {
      case AuctionDayAheadType.CAPACITY:
        return type === 'price' ? 'PLN/kW' : 'kW';
      case AuctionDayAheadType.ENERGY:
        return type === 'price' ? 'PLN/kWh' : 'kWh';
    }
  }

  getOffersTooltip(row: AuctionDayAheadDTO): string {
    const content = row.offers
      .map(({ companyName, potentialId, potentialName, volume, price }) => {
        const content = this.translate.instant('auctions.table.offerDayAheadTooltip', {
          companyName,
          potentialId,
          potentialName,
          volume,
          price,
          priceUnit: this.getUnit('price', row),
          volumeUnit: this.getUnit('volume', row),
        });

        return `<li>${content}</li>`;
      })
      .join('');

    return row.offers.length ? `<ol>${content}</ol>` : '';
  }

  getTabs(): Tab[] {
    return this.service.getTabs();
  }

  getStatus(status: AuctionStatus): AuctionStatus {
    if ([AuctionStatus.NEW_CAPACITY, AuctionStatus.NEW_ENERGY].includes(status)) {
      return AuctionStatus.NEW;
    }

    if ([AuctionStatus.CLOSED_CAPACITY, AuctionStatus.CLOSED_ENERGY].includes(status)) {
      return AuctionStatus.CLOSED;
    }

    if ([AuctionStatus.OPEN_CAPACITY, AuctionStatus.OPEN_ENERGY].includes(status)) {
      return AuctionStatus.OPEN;
    }

    return status;
  }

  hasStatus(status: AuctionStatus, row: AuctionDayAheadDTO): boolean {
    return this.service.hasStatus(status, row);
  }

  preview(row: AuctionDayAheadDTO | AuctionsSeriesDTO): void {
    this.modalService.open(DayAheadPreviewComponent, {
      data: {
        model: row,
        tab: this.selectedTab,
        type: AuctionType.DAY_AHEAD,
        isSeriesView: this.isSeriesView,
      },
      styleClass: 'full-view',
    });
  }

  tabChange(): void {
    this.dictionaries.statuses = this.service.getStatusDictionary(this.selectedTab);

    switch (this.selectedTab) {
      case 'energy-auctions':
        this.sort = 'statusCode,energyGateClosureTime,asc';
        break;
      case 'capacity-auctions':
        this.sort = 'statusCode,capacityGateClosureTime,asc';
        break;
      default:
        this.sort = 'id,desc';
        break;
    }

    this.loadViewConfiguration();
    this.preparedColumnsForTab();
    this.getCollection();
  }

  private preparedColumnsForTab(): void {
    this.columns = this.preparedColumns(COLUMNS, 'auctions.table').filter(({ field }) => {
      let fields: string[] = [];

      switch (this.selectedTab) {
        case 'energy-auctions':
          fields = [
            'minDesiredCapacity',
            'maxDesiredCapacity',
            'capacityGateOpeningTime',
            'capacityGateClosureTime',
            'capacityAvailability',
          ];
          break;
        case 'capacity-auctions':
          fields = ['minDesiredEnergy', 'maxDesiredEnergy', 'energyGateOpeningTime', 'energyGateClosureTime', 'energyAvailability'];
          break;
      }

      if (!this.isSeriesView) {
        fields = [
          ...fields,
          'type',
          'firstAuctionDate',
          'lastAuctionDate',
          'createdDate',
          'lastModifiedDate',
          'createdBy',
          'lastModifiedBy',
          'delete',
        ];
      } else {
        fields = [...fields, 'status', 'deliveryDate', 'offers'];
      }

      return !fields.includes(field);
    });

    this.onActiveColumnsChange(false);
  }

  private watchRxStomp(): void {
    this.rxStompService
      .watch(`/refresh-view/auctions/day-ahead`)
      .pipe(takeUntil(this.destroy$))
      .subscribe(message => {
        const data: AuctionDayAheadDTO = JSON.parse(message.body || '');

        if (!!data) {
          this.getCollection();
        }
      });
  }
}
