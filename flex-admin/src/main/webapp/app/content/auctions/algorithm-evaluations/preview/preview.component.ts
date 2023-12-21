import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { Component, ElementRef, Injector } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Helpers } from '@app/shared/commons';
import { AuctionOfferStatus, AuctionDayAheadType } from '../../enums';
import { TableExtends } from '@app/shared/services';
import { AuctionOffersParameters } from '../../offers/offers.store';
import { SessionStorageService } from '@app/core';
import { ViewConfigurationService } from '@app/shared/commons/view-configuration';
import { BidsEvaluationDTO } from '../../bids-evaluation/bids-evaluation';
import { AuctionsService } from '../../auctions.service';
import { COLUMNS } from './preview.columns';
import { OffersPreviewStore } from './preview.store';

@Component({
  selector: 'app-auctions-algorithm-evaluations-preview',
  templateUrl: './preview.component.html',
  styleUrls: ['./preview.component.scss'],
  providers: [OffersPreviewStore],
})
export class AlgorithmEvaluationsPreviewComponent extends TableExtends {
  viewName: string | undefined;
  data$ = this.store.data$;
  dataIds$ = this.store.dataIds$;
  totalRecords$ = this.store.totalRecords$;
  parameters: AuctionOffersParameters | undefined;
  columns = this.preparedColumns(COLUMNS, 'auctions.offers');

  dictionaries = {
    statuses: Helpers.enumToDictionary(AuctionOfferStatus, 'AuctionOfferStatus'),
  };

  get evaluationId(): number {
    return this.config.data.id;
  }

  constructor(
    injector: Injector,
    route: ActivatedRoute,
    elementRef: ElementRef,
    sessionStorage: SessionStorageService,
    public router: Router,
    public ref: DynamicDialogRef,
    public config: DynamicDialogConfig,
    private auctionService: AuctionsService,
    private store: OffersPreviewStore,
    protected viewConfigurationService: ViewConfigurationService
  ) {
    super(injector, route, elementRef, sessionStorage, viewConfigurationService);
  }

  getCollection(): void {
    const dateTimeKeys = ['deliveryPeriod'];

    this.parameters = {
      page: this.page,
      size: this.rows,
      sort: this.sort,
      evaluationId: this.evaluationId,
      auctionCmvcId: undefined,
      auctionDayAheadId: undefined,
      filters: Helpers.serializeFilters(this.staticFilters, this.dynamicFilters, dateTimeKeys),
    };

    this.store.loadCollection({
      ...this.parameters,
      runAfterGetData: () => this.updateHandyScroll(),
    });
  }

  getUnit(type: 'price' | 'volume', row: BidsEvaluationDTO): string {
    switch (row.type) {
      case AuctionDayAheadType.CAPACITY:
        return type === 'price' ? 'PLN/kW' : 'kW';
      case AuctionDayAheadType.ENERGY:
        return type === 'price' ? 'PLN/kWh' : 'kWh';
    }
  }

  formatAcceptedDeliveryDate(row: BidsEvaluationDTO): string {
    return this.auctionService.formatDeliveryDate(row.acceptedDeliveryPeriodFrom, row.acceptedDeliveryPeriodTo);
  }

  formatDeliveryDate(row: BidsEvaluationDTO): string {
    return this.auctionService.formatDeliveryDate(row.deliveryPeriodFrom, row.deliveryPeriodTo);
  }

  close(): void {
    this.ref.close();
  }
}
