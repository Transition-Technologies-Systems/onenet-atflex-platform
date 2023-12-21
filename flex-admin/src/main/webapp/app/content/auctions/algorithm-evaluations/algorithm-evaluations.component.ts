import { ActivatedRoute, Router } from '@angular/router';
import { AfterViewInit, Component, ElementRef, Injector, ViewChild } from '@angular/core';
import { AlgorithmEvaluationsStore } from './algorithm-evaluations.store';
import { AppToastrService, DownloadService, SessionStorageService, ToastrMessage } from '@app/core';
import { Helpers, ModalService } from '@app/shared/commons';

import { AlgorithmEvaluationDTO, AlgorithmStatus, AlgorithmType } from './algorithm-evaluation';
import { AlgorithmEvaluationsPreviewComponent } from './preview';
import { AlgorithmEvaluationsProcessLogsComponent } from './process-logs';
import { AlgorithmEvaluationsService } from './algorithm-evaluations.service';
import { COLUMNS } from './algorithm-evaluations.columns';
import { FileUpload } from 'primeng/fileupload';
import { HttpErrorResponse } from '@angular/common/http';
import { RxStompService } from '@stomp/ng2-stompjs';
import { TableExtends } from '@app/shared/services';
import { ViewConfigurationService } from '@app/shared/commons/view-configuration';
import { catchError, debounceTime, takeUntil, tap } from 'rxjs';
import { ConfirmationService } from 'primeng/api';
import { TranslateService } from '@ngx-translate/core';
import { DefaultParameters, FileDTO, SplittedMenuItem } from '@app/shared/models';
import { ContentType } from '@app/shared/enums';
import { MinimalDTO } from '../../../shared/models/minimal';
import { AuctionsService } from '../auctions.service';
import { AuctionEmailDTO } from '../enums/auction-email-category';

@Component({
  selector: 'app-algorithm-evaluations',
  templateUrl: './algorithm-evaluations.component.html',
  styleUrls: ['./algorithm-evaluations.component.scss'],
  providers: [AlgorithmEvaluationsStore],
})
export class AlgorithmEvaluationsComponent extends TableExtends implements AfterViewInit {
  @ViewChild(FileUpload) fileUploadEl: FileUpload | null = null;

  viewName = 'auction-algorithm-evaluations';

  data$ = this.store.data$;
  totalRecords$ = this.store.totalRecords$;
  parameters: DefaultParameters | undefined;
  columns = this.preparedColumns(COLUMNS, 'algorithmEvaluations.table');
  dictionaries = {
    typeOfAlgorithms: Helpers.enumToDictionary(AlgorithmType, 'AlgorithmType'),
    kdmModelNames: this.service.getKDMModelsDictionary(),
  };

  splittedMenuItems: SplittedMenuItem[] = [];

  constructor(
    injector: Injector,
    route: ActivatedRoute,
    elementRef: ElementRef,
    public router: Router,
    private toastr: AppToastrService,
    sessionStorage: SessionStorageService,
    private modalService: ModalService,
    private rxStompService: RxStompService,
    private store: AlgorithmEvaluationsStore,
    private service: AlgorithmEvaluationsService,
    private auctionsService: AuctionsService,
    private confirmationService: ConfirmationService,
    private translate: TranslateService,
    protected viewConfigurationService: ViewConfigurationService
  ) {
    super(injector, route, elementRef, sessionStorage, viewConfigurationService);
  }

  ngAfterViewInit(): void {
    this.subscribeLangChange();
    this.afterViewInit();
    this.watchRxStomp();
  }

  subscribeLangChange(): void {
    this.translate.onLangChange
      .pipe(
        takeUntil(this.destroy$),
        debounceTime(500),
        tap(() => {
          if (this.parameters?.sort.includes('typeOfAlgorithm')) {
            this.getCollection();
          }
        })
      )
      .subscribe();
  }

  generateResults(row: AlgorithmEvaluationDTO): void {
    this.service.generateResults(row.evaluationId);
  }

  downloadInput(row: AlgorithmEvaluationDTO): void {
    this.service.downloadInput(row.evaluationId);
  }

  downloadOutput(row: AlgorithmEvaluationDTO): void {
    this.service.downloadOutput(row.evaluationId);
  }

  getCollection(): void {
    const dateTimeKeys = ['deliveryDate', 'creationDate', 'endDate'];

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

  parseResult(row: AlgorithmEvaluationDTO): void {
    const service =
      row.typeOfAlgorithm === 'DANO' ? this.service.parseDANOResult(row.evaluationId) : this.service.parsePBCMResult(row.evaluationId);
    service
      .pipe(
        catchError(({ status, error }: HttpErrorResponse): any => {
          const errorsWithStringifyParams = [
            'error.algorithm.import.notUpdatedDerPresent',
            'error.algorithm.import.severalNotUpdatedDersPresent',
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

          this.toastr.error('algorithmEvaluations.actions.parseResult.error');
        })
      )
      .subscribe(() => {
        this.toastr.success('algorithmEvaluations.actions.parseResult.success');
      });
  }

  cancelEvaluation(row: AlgorithmEvaluationDTO): void {
    this.service
      .cancelEvaluation(row.evaluationId)
      .pipe(
        tap(() => {
          this.toastr.success('algorithmEvaluations.actions.cancelEvaluation.success');
        }),
        catchError((err: HttpErrorResponse): any => {
          if (!(err.status === 400 && err.error?.errorKey)) {
            this.toastr.error('algorithmEvaluations.actions.cancelEvaluation.error');
            return;
          }
        })
      )
      .subscribe();
  }

  cancelEvaluationConfirm(event: Event, row: AlgorithmEvaluationDTO): void {
    this.confirmationService.confirm({
      target: event.target || undefined,
      message: this.translate.instant('algorithmEvaluations.actions.cancelEvaluation.question'),
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.cancelEvaluation(row);
      },
    });
  }

  exportEvaluation(row: AlgorithmEvaluationDTO): void {
    this.service
      .exportEvaluation(row.evaluationId)
      .pipe(
        tap(({ fileName, base64StringData }: FileDTO) => {
          DownloadService.saveFileWithParam(base64StringData, fileName, ContentType.XLSX, true);
        })
      )
      .subscribe();
  }

  preview(row: AlgorithmEvaluationDTO): void {
    this.modalService.open(AlgorithmEvaluationsPreviewComponent, {
      data: {
        id: row.evaluationId,
      },
    });
  }

  previewLogs(row: AlgorithmEvaluationDTO): void {
    this.modalService.open(AlgorithmEvaluationsProcessLogsComponent, {
      data: {
        id: row.evaluationId,
      },
    });
  }

  updateSplittedMenuItems(row: AlgorithmEvaluationDTO) {
    const { typeOfAlgorithm, status, evaluationId } = row;
    let emailCategory = '';
    if (typeOfAlgorithm === 'BM' && status === 'COMPLETED') {
      emailCategory = 'ALGORITHM_RESULT';
    } else if (typeOfAlgorithm === 'PBCM' || typeOfAlgorithm === 'DANO') {
      emailCategory = 'BID';
    }
    this.splittedMenuItems = [
      {
        label: this.translate.instant('auctions.actions.sendViaEmail.menuItem'),
        icon: 'pi pi-fw pi-envelope',
        command: (event: any) => {
          if (event) {
            this.auctionsService
              .sendPositionViaEmail(emailCategory, evaluationId)
              .pipe(
                tap((response: AuctionEmailDTO) => {
                  this.toastr.success(
                    new ToastrMessage({
                      msg: this.translate.instant('auctions.actions.sendViaEmail.success', { email: response.notifiedEmailAdress }),
                    })
                  );
                }),
              )
              .subscribe();
          }
        },
      },
    ];
  }

  getBidTooltip(row: AlgorithmEvaluationDTO): string {
    const { offers } = row;
    const bidIdTranslate = this.translate.instant('algorithmEvaluations.tooltip.bidId');
    const schedulingUnitTranslate = this.translate.instant('algorithmEvaluations.tooltip.schedulingUnit');
    const bidStatusTranslate = this.translate.instant('algorithmEvaluations.tooltip.bidStatus');
    if (offers?.length) {
      return `<ul> ${offers
        .map(item => {
          return `<br> <li>${bidIdTranslate}: ${item.id} <br> ${schedulingUnitTranslate}: ${
            item.potentialName
          } <br> ${bidStatusTranslate}: ${this.translate.instant('AuctionOfferStatus.' + item.status)} <br>`;
        })
        .join('</li>')} </ul>`;
    }

    return '';
  }

  private watchRxStomp(): void {
    this.rxStompService
      .watch('/refresh-view/auctions/algorithm-evaluations')
      .pipe(takeUntil(this.destroy$))
      .subscribe(message => {
        const data: AlgorithmEvaluationDTO = JSON.parse(message.body || '');

        if (!!data && data.evaluationId) {
          this.store.upsertOne(data);
        }
      });

    this.rxStompService
      .watch('/refresh-view/auctions/algorithm-evaluations/status')
      .pipe(takeUntil(this.destroy$))
      .subscribe(message => {
        const data: MinimalDTO<number, AlgorithmStatus> = JSON.parse(message.body || '');

        if (!!data && data.id) {
          this.store.upsertStatus(data);
        }
      });
  }
}
