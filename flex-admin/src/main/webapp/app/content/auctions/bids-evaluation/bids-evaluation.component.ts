import * as moment from 'moment';

import { ActivatedRoute, Router } from '@angular/router';
import { AppToastrService, SessionStorageService, ToastrMessage } from '@app/core';
import { AuctionOfferStatus, AuctionDayAheadType, AuctionStatusSimplified, AuctionType } from '../enums';
import { BidsEvaluationStore } from './bids-evaluation.store';
import { Component, ElementRef, Injector } from '@angular/core';
import { Helpers, ModalService } from '@app/shared/commons';

import { AlgorithmType } from '@app/shared/enums';
import { AuctionBidModalComponent } from '../bid-dialog';
import { AuctionsService } from '../auctions.service';
import { BidsEvaluationDTO } from './bids-evaluation';
import { BidsEvaluationExportDialogComponent } from './export';
import { BidsEvaluationRunAlgorithmDialogComponent } from './run-algorithm';
import { BidsEvaluationService } from './bids-evaluation.service';
import { COLUMNS } from './bids-evaluation.columns';
import { FileUpload } from 'primeng/fileupload';
import { HttpErrorResponse } from '@angular/common/http';
import { TableExtends } from '@app/shared/services';
import { ViewConfigurationService } from '@app/shared/commons/view-configuration';
import { catchError, firstValueFrom, tap } from 'rxjs';
import { TranslateService } from '@ngx-translate/core';
import { DefaultParameters, SplittedMenuItem } from '@app/shared/models';
import { AuctionEmailDTO } from '../enums/auction-email-category';

@Component({
  selector: 'app-bids-evaluation',
  templateUrl: './bids-evaluation.component.html',
  styleUrls: ['./bids-evaluation.component.scss'],
  providers: [BidsEvaluationStore],
})
export class BidsEvaluationComponent extends TableExtends {
  viewName = 'auction-bids-evaluation';

  data$ = this.store.data$;
  dataIds$ = this.store.dataIds$;
  totalRecords$ = this.store.totalRecords$;
  seperateFilterDates = ['deliveryPeriod'];
  filtersWithDateToNextDay = ['deliveryPeriod'];
  parameters: DefaultParameters | undefined;
  columns = this.preparedColumns(COLUMNS, 'auctions.offers');

  selectedRow: { [id: number]: boolean } = {};
  allSelectedRows: BidsEvaluationDTO[] = [];
  algorithmTypes: typeof AlgorithmType = AlgorithmType;
  auctionOfferStatus: typeof AuctionOfferStatus = AuctionOfferStatus;
  splittedMenuItems: SplittedMenuItem[] = [];

  dictionaries = {
    statuses: Helpers.enumToDictionary(AuctionOfferStatus, 'AuctionOfferStatus'),
    auctionStatuses: Helpers.enumToDictionary(AuctionStatusSimplified, 'AuctionStatus'),
  };

  private currentFilters: any = {};

  constructor(
    injector: Injector,
    route: ActivatedRoute,
    elementRef: ElementRef,
    public router: Router,
    private toastr: AppToastrService,
    sessionStorage: SessionStorageService,
    private modalService: ModalService,
    private store: BidsEvaluationStore,
    private service: BidsEvaluationService,
    private auctionService: AuctionsService,
    private translate: TranslateService,
    protected viewConfigurationService: ViewConfigurationService
  ) {
    super(injector, route, elementRef, sessionStorage, viewConfigurationService);
  }

  edit(row: BidsEvaluationDTO): void {
    const refresh = () => {
      const parameters = this.parameters as DefaultParameters;

      this.store.loadCollection({
        ...parameters,
        refresh: true,
        runAfterGetData: () => this.updateHandyScroll(),
      });
    };

    this.modalService
      .open(AuctionBidModalComponent, {
        data: { auctionName: row.auctionName, bid: row, id: row.id, type: row.offerCategory, fromBids: true },
        styleClass: 'full-view',
      })
      .onClose.subscribe(() => refresh());
  }

  async export(isSETO = false, isSendEmail = false, emailCategory: string = ''): Promise<void> {
    const selectedRows = await this.getSelectedRow();
    const offers = selectedRows.map(({ id }) => id);

    if (!offers.length) {
      this.modalService.open(BidsEvaluationExportDialogComponent, {
        data: {
          isSETO,
          isSendEmail,
          emailCategory,
        },
      });
      return;
    }

    let filters = {
      'id.in': offers,
      'auctionCategoryAndType.in': ['CMVC_CAPACITY', 'DAY_AHEAD_ENERGY', 'DAY_AHEAD_CAPACITY'],
    };

    if (isSendEmail) {
      this.auctionService
        .sendPositionViaEmail(emailCategory, null, filters)
        .pipe(
          tap((response: AuctionEmailDTO) => {
            this.toastr.success(
              new ToastrMessage({
                msg: this.translate.instant('auctions.actions.sendViaEmail.success', { email: response.notifiedEmailAdress }),
              })
            );
          })
        )
        .subscribe();
    } else {
      this.service.exportXLSX(filters, isSETO);
    }
  }

  formatAcceptedDeliveryDate(row: BidsEvaluationDTO): string {
    return this.auctionService.formatDeliveryDate(row.acceptedDeliveryPeriodFrom, row.acceptedDeliveryPeriodTo);
  }

  formatDeliveryDate(row: BidsEvaluationDTO): string {
    return this.auctionService.formatDeliveryDate(row.deliveryPeriodFrom, row.deliveryPeriodTo);
  }

  getCollection(): void {
    const dateTimeKeys = ['deliveryPeriod'];

    this.currentFilters = Helpers.serializeFilters(this.staticFilters, this.dynamicFilters, dateTimeKeys);

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

  getRowClass(row: BidsEvaluationDTO): string {
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

  getUnit(type: 'price' | 'volume', row: BidsEvaluationDTO): string {
    switch (row.type) {
      case AuctionDayAheadType.CAPACITY:
        return type === 'price' ? 'PLN/kW' : 'kW';
      case AuctionDayAheadType.ENERGY:
        return type === 'price' ? 'PLN/kWh' : 'kWh';
    }
  }

  import({ currentFiles }: any, fileInput: FileUpload, typeOfImport: string): void {
    const formData = new FormData();
    formData.append('file', currentFiles[0]);

    this.service
      .import(formData, typeOfImport)
      .pipe(
        catchError((response: HttpErrorResponse): any => {
          fileInput.clear();
          const { status, error } = response;

          const errorsWithStringifyParams = [
            'error.auction.offer.import.notUpdatedDerPresent',
            'error.auction.offer.import.severalNotUpdatedDersPresent',
            'error.auction.offer.import.activePowerOutOfRange',
          ];

          if (status === 400 && error?.errorKey) {
            if (errorsWithStringifyParams.includes(error?.errorKey)) {
              const params = JSON.parse(error?.params);
              const message = this.translate.instant(error?.errorKey, { ...params });
              this.toastr.error(message);
            } else {
              this.toastr.error(error?.errorKey);
            }
            return;
          }

          this.toastr.error(`auctions.actions.bidsEvaluation.${typeOfImport === 'AGNO' ? 'importAGNO' : 'import'}.error`);
        })
      )
      .subscribe((response: any) => {
        if (typeOfImport !== 'AGNO') {
          const { importedBids, notImportedBids } = response;

          const type =
            importedBids.length && !notImportedBids.length
              ? 'success'
              : importedBids.length && notImportedBids.length
              ? 'partialSuccess'
              : 'failed';

          const toastrType = type === 'success' ? 'success' : type === 'failed' ? 'error' : 'warning';

          const message = new ToastrMessage({
            msg: `auctions.actions.bidsEvaluation.import.${type}`,
            params: {
              importedBids: importedBids.length ? importedBids.join(', ') : null,
              notImportedBids: `<ul> ${notImportedBids
                .map((item: any) => `<li>${item.id}: ${this.translate.instant(item.value)}`)
                .join('</li>')} </ul>`,
            },
          });
          this.toastr[toastrType](message);
        } else {
          this.toastr.success('auctions.actions.bidsEvaluation.runAlgorithm.success');
        }

        fileInput.clear();
        this.getCollection();
      });
  }

  updateSplittedMenuItems(isSETO = false) {
    let emailCategory = isSETO ? 'DSO_SETO_EXPORT' : 'TSO_EXPORT';
    this.splittedMenuItems = [
      {
        label: this.translate.instant('auctions.actions.sendViaEmail.menuItem'),
        icon: 'pi pi-fw pi-envelope',
        command: (c: any) => {
          this.export(isSETO, true, emailCategory);
        },
      },
    ];
  }

  getSchedulingUnitOrPotentialTooltip({ offerCategory, derMinDTOs, flexibilityPotentialVolume }: BidsEvaluationDTO): string {
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

  async mark(key: 'accepted' | 'rejected', status: AuctionOfferStatus): Promise<void> {
    const rows = await this.getSelectedRow();

    if (!rows.length) {
      this.toastr.warning('auctions.actions.bidsEvaluation.noSelected');
      return;
    }

    this.service
      .mark(status, rows)
      .pipe(
        catchError((response: HttpErrorResponse): any => {
          if (!(response.status === 400 && response.error?.errorKey)) {
            this.toastr.error(`auctions.actions.bidsEvaluation.${key}.error`);
            return;
          }
        })
      )
      .subscribe(() => {
        this.toastr.success(`auctions.actions.bidsEvaluation.${key}.success`);

        this.getCollection();
      });
  }

  async runAlgorithm(algorithmType: AlgorithmType): Promise<void> {
    const deliveryPeriod = this.currentFilters?.['deliveryPeriod.greaterThanOrEqual'];
    const selectedRows = await this.getSelectedRow();
    const offers = selectedRows.map(({ id }) => id);

    if (!deliveryPeriod) {
      this.toastr.warning('auctions.actions.bidsEvaluation.runAlgorithm.noDeliveryDate');
      return;
    }

    const deliveryDate = moment(deliveryPeriod).utc().format();

    this.modalService.open(BidsEvaluationRunAlgorithmDialogComponent, {
      data: {
        algorithmType,
        deliveryDate,
        offers,
      },
    });
  }

  async setSelectedRows(event: boolean, row: BidsEvaluationDTO): Promise<void> {
    if (event) {
      const data: BidsEvaluationDTO[] = await firstValueFrom(this.store.data$);
      const itemId = data.findIndex(item => item.id === row.id);
      this.allSelectedRows.push(data[itemId]);
    } else {
      this.allSelectedRows.splice(
        this.allSelectedRows.findIndex((item: BidsEvaluationDTO) => item.id === row.id),
        1
      );
    }
  }

  private async getSelectedRow(): Promise<BidsEvaluationDTO[]> {
    return this.allSelectedRows;
  }
}
