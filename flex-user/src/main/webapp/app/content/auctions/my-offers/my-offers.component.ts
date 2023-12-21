import { ActivatedRoute, Router } from '@angular/router';
import { SessionStorageService } from '@app/core';
import { AuctionDayAheadType, AuctionOfferStatus, AuctionStatus, AuctionType } from '../enums';
import { Component, ElementRef, Injector, Input } from '@angular/core';
import { Helpers, ModalService } from '@app/shared/commons';
import { MyOffersStore } from './my-offers.store';

import { AuctionBidModalComponent } from '../bid-dialog';
import { AuctionsService } from '../auctions.service';
import { BooleanEnum } from '@app/shared/enums';
import { COLUMNS } from './my-offers.columns';
import { MyOffersDTO } from './my-offers';
import { MyOffersService } from './my-offers.service';
import { TableExtends } from '@app/shared/services';
import { ViewConfigurationService } from '@app/shared/commons/view-configuration';
import { TranslateService } from '@ngx-translate/core';
import { DefaultParameters } from '@app/shared/models';

@Component({
  selector: 'app-auctions-my-offers',
  templateUrl: './my-offers.component.html',
  styleUrls: ['./my-offers.component.scss'],
  providers: [MyOffersStore, MyOffersService],
})
export class MyOffersComponent extends TableExtends {
  @Input() auctionType: AuctionType = AuctionType.CMVC;

  viewName = 'auction-my-offers';

  data$ = this.store.data$;
  dataIds$ = this.store.dataIds$;
  totalRecords$ = this.store.totalRecords$;
  seperateFilterDates = ['deliveryPeriod', 'acceptedDeliveryPeriod'];
  filtersWithDateToNextDay = ['deliveryPeriod', 'acceptedDeliveryPeriod'];
  parameters: DefaultParameters | undefined;
  columns = this.preparedColumns(COLUMNS, 'auctions.offers');

  selectedRow: { [id: number]: boolean } = {};
  auctionOfferStatus: typeof AuctionOfferStatus = AuctionOfferStatus;

  dictionaries = {
    statuses: Helpers.enumToDictionary(AuctionOfferStatus, 'AuctionOfferStatus'),
    auctionStatuses: Helpers.enumToDictionary<AuctionStatus>(AuctionStatus, 'AuctionStatus')
      .filter(({ value }) => {
        return [AuctionStatus.OPEN, AuctionStatus.CLOSED, AuctionStatus.NEW].includes(value);
      })
      .map(({ value, label }) => ({
        label,
        value: `${value},${value}_CAPACITY,${value}_ENERGY`,
      })),
    boolean: Helpers.enumToDictionary(BooleanEnum, 'Boolean'),
  };

  constructor(
    injector: Injector,
    route: ActivatedRoute,
    elementRef: ElementRef,
    public router: Router,
    sessionStorage: SessionStorageService,
    private modalService: ModalService,
    private store: MyOffersStore,
    private auctionService: AuctionsService,
    protected viewConfigurationService: ViewConfigurationService,
    private translate: TranslateService
  ) {
    super(injector, route, elementRef, sessionStorage, viewConfigurationService);
  }

  edit(row: MyOffersDTO): void {
    this.modalService
      .open(AuctionBidModalComponent, {
        data: { auctionName: row.auctionName, bid: row, id: row.id, type: row.offerCategory, fromBids: true },
        styleClass: 'full-view',
      })
      .onClose.subscribe(() => {
        const parameters = this.parameters as DefaultParameters;

        this.store.loadCollection({
          ...parameters,
          refresh: true,
          runAfterGetData: () => this.updateHandyScroll(),
        });
      });
  }

  formatAcceptedDeliveryDate(row: MyOffersDTO): string {
    return this.auctionService.formatDeliveryDate(row.acceptedDeliveryPeriodFrom, row.acceptedDeliveryPeriodTo);
  }

  formatDeliveryDate(row: MyOffersDTO): string {
    return this.auctionService.formatDeliveryDate(row.deliveryPeriodFrom, row.deliveryPeriodTo);
  }

  getCollection(): void {
    const dateTimeKeys = ['deliveryPeriod', 'acceptedDeliveryPeriod'];

    this.selectedRow = {};

    this.parameters = {
      page: this.page,
      size: this.rows,
      sort: this.sort,
      filters: {
        ...Helpers.serializeFilters(this.staticFilters, this.dynamicFilters, dateTimeKeys),
        'offerCategory.in': this.auctionType,
      },
    };

    this.store.loadCollection({
      ...this.parameters,
      runAfterGetData: () => this.updateHandyScroll(),
    });
  }

  getRowClass(row: MyOffersDTO): string {
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

  getUnit(type: 'price' | 'volume', row: MyOffersDTO): string {
    switch (row.type) {
      case AuctionDayAheadType.CAPACITY:
        return type === 'price' ? 'PLN/kW' : 'kW';
      case AuctionDayAheadType.ENERGY:
        return type === 'price' ? 'PLN/kWh' : 'kWh';
    }
  }

  getSchedulingUnitOrPotentialTooltip({ offerCategory, derMinDTOs, flexibilityPotentialVolume }: MyOffersDTO): string {
    let potential = '';
    if (offerCategory === AuctionType.CMVC) {
      potential = `${this.translate.instant('auctions.offers.tooltip.potential')}: ${flexibilityPotentialVolume}`;
    }
    return `${potential}
      ${this.translate.instant('auctions.offers.tooltip.ders')}:
       <ul> ${derMinDTOs
         .map(item => {
           return `<li>${item.name}`;
         })
         .join('</li>')} </ul>`;
  }
}
