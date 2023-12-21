import { AfterViewInit, Component, ElementRef, Injector } from '@angular/core';
import { AppToastrService, SessionStorageService } from '@app/core';
import { AuctionCmvcDTO, Tab, TabType } from './cm-vc';
import { AuctionStatus, AuctionType } from '../enums';
import { CmVcParameters, CmVcStore } from './cm-vc.store';
import { Helpers, ModalService } from '@app/shared/commons';

import { ActivatedRoute } from '@angular/router';
import { AuctionBidModalComponent } from '../bid-dialog';
import { COLUMNS } from './cm-vc.columns';
import { CmVcDialogComponent } from './dialog';
import { CmVcPreviewComponent } from './preview';
import { CmVcService } from '.';
import { ConfirmationService } from 'primeng/api';
import { Dictionary } from '@app/shared/models';
import { HttpErrorResponse } from '@angular/common/http';
import { RxStompService } from '@stomp/ng2-stompjs';
import { TableExtends } from '@app/shared/services';
import { TranslateService } from '@ngx-translate/core';
import { ViewConfigurationService } from '@app/shared/commons/view-configuration';
import { takeUntil, catchError } from 'rxjs';

interface Dictionaries {
  statuses: Dictionary[];
}

@Component({
  selector: 'app-auctions-cm-vc',
  templateUrl: './cm-vc.component.html',
  providers: [CmVcStore],
})
export class CmVcComponent extends TableExtends implements AfterViewInit {
  viewName = 'auctions-cmvc';
  selectedTab: TabType = 'auctions';
  sort = 'statusCode,gateClosureTime,asc';

  data$ = this.store.data$;
  dataIds$ = this.store.dataIds$;
  totalRecords$ = this.store.totalRecords$;
  parameters: CmVcParameters | undefined;
  columns = this.preparedColumns(COLUMNS, 'auctions.table');

  seperateFilterDates = ['deliveryDate'];
  filtersWithDateToNextDay = ['deliveryDate'];
  setRangeFilterDateTime = ['createdDate', 'lastModifiedDate'];
  filtersWithTime = ['gateOpeningTime', 'gateClosureTime'];

  dictionaries: Dictionaries = {
    statuses: Helpers.enumToDictionary<AuctionStatus>(AuctionStatus, 'AuctionStatus').filter(({ value }) => {
      return [AuctionStatus.OPEN, AuctionStatus.CLOSED, AuctionStatus.NEW].includes(value);
    }),
  };

  get showFilterMenuForDeiveryDate(): boolean {
    if (!this.viewParameters.dynamicFilters.deliveryDate) {
      return true;
    }

    const value = this.viewParameters.dynamicFilters.deliveryDate?.value ?? [];

    return value.filter(Boolean).length < 2;
  }

  constructor(
    injector: Injector,
    route: ActivatedRoute,
    elementRef: ElementRef,
    private store: CmVcStore,
    private service: CmVcService,
    private toastr: AppToastrService,
    sessionStorage: SessionStorageService,
    private modalService: ModalService,
    private translate: TranslateService,
    private rxStompService: RxStompService,
    private confirmationService: ConfirmationService,
    protected viewConfigurationService: ViewConfigurationService
  ) {
    super(injector, route, elementRef, sessionStorage, viewConfigurationService);
  }

  ngAfterViewInit(): void {
    this.afterViewInit();

    this.watchRxStomp();
  }

  add(): void {
    const dialog = this.modalService.open(CmVcDialogComponent, { styleClass: 'full-view' });

    dialog.onClose.pipe(takeUntil(this.destroy$)).subscribe((result: boolean) => {
      if (!!result) {
        this.getCollection();
      }
    });
  }

  addBid(row: AuctionCmvcDTO): void {
    this.service
      .getAuction(row.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe(resp => {
        this.modalService
          .open(AuctionBidModalComponent, { data: { model: resp, type: AuctionType.CMVC }, styleClass: 'full-view' })
          .onClose.subscribe(() => {
            this.getCollection();
          });
      });
  }

  delete(event: Event, row: AuctionCmvcDTO): void {
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
              }
              return;
            })
          )
          .subscribe(() => {
            this.toastr.success('auctions.actions.delete.success');
            this.getCollection();
          });
      },
    });
  }

  edit(row: AuctionCmvcDTO): void {
    const dialog = this.modalService.open(CmVcDialogComponent, { data: row, styleClass: 'full-view' });

    dialog.onClose.pipe(takeUntil(this.destroy$)).subscribe((result: boolean) => {
      if (!!result) {
        this.getCollection();
      }
    });
  }

  formatDeliveryDate(row: AuctionCmvcDTO): string {
    return this.service.formatDeliveryDate(row);
  }

  formatLoalizations(row: AuctionCmvcDTO): string {
    return this.service.formatLoalizations(row);
  }

  getCollection(): void {
    const dateTimeKeys = ['createdDate', 'lastModifiedDate', 'gateOpeningTime', 'gateClosureTime', 'deliveryDateFrom', 'deliveryDateTo'];

    this.parameters = {
      page: this.page,
      size: this.rows,
      sort: this.sort,
      filters: Helpers.serializeFilters(this.staticFilters, this.dynamicFilters, dateTimeKeys),
    };

    this.store.loadCollection({
      ...this.parameters,
      runAfterGetData: () => this.updateHandyScroll(),
    });
  }

  getUnit(type: 'price' | 'volume'): string {
    return type === 'price' ? 'PLN/kW' : 'kW';
  }

  getOffersTooltip(row: AuctionCmvcDTO): string {
    const content = row.offers
      .map(({ companyName, potentialId, potentialName, volume, price }) => {
        const content = this.translate.instant('auctions.table.offerCmvcTooltip', {
          companyName,
          potentialId,
          potentialName,
          volume,
          price,
          priceUnit: this.getUnit('price'),
          volumeUnit: this.getUnit('volume'),
        });

        return `<li>${content}</li>`;
      })
      .join('');

    return row.offers.length ? `<ol>${content}</ol>` : '';
  }

  getTabs(): Tab[] {
    return this.service.getTabs();
  }

  preview(row: AuctionCmvcDTO): void {
    this.modalService.open(CmVcPreviewComponent, {
      data: {
        model: row,
        type: AuctionType.CMVC,
      },
      styleClass: 'full-view',
    });
  }

  tabChange(): void {
    switch (this.selectedTab) {
      case 'auctions':
        this.sort = 'statusCode,gateClosureTime,asc';
        break;
      default:
        this.sort = 'id,desc';
        break;
    }

    this.loadViewConfiguration();
    this.getCollection();
  }

  private watchRxStomp(): void {
    this.rxStompService
      .watch(`/refresh-view/auctions/cmvc`)
      .pipe(takeUntil(this.destroy$))
      .subscribe(message => {
        const data: AuctionCmvcDTO = JSON.parse(message.body || '');

        if (!!data) {
          this.getCollection();
        }
      });
  }
}
