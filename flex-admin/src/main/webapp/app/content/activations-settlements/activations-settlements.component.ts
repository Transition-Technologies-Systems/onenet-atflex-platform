import { catchError } from 'rxjs';
import { Component, ElementRef, Injector, ViewChild } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { AppToastrService, SessionStorageService } from '@app/core';
import { Helpers, ModalService } from '@app/shared/commons';
import { ViewConfigurationService } from '@app/shared/commons/view-configuration';
import { ActivationsSettlementsDTO } from '@app/shared/models/activations-settlements';
import { TableExtends } from '@app/shared/services';
import { TranslateService } from '@ngx-translate/core';
import { COLUMNS } from './activations-settlements.columns';
import { ActivationsSettlementsService } from './activations-settlements.service';
import { ActivationsSettlementsStore } from './activations-settlements.store';
import { ActivationsSettlementsDialogComponent } from './dialog/dialog.component';
import { ActivationsSettlementsPreviewComponent } from './preview/preview.component';
import { FileUpload } from 'primeng/fileupload';
import { ConfirmationService } from 'primeng/api';
import { CustomConfirmComponent } from '@app/shared/commons/custom-confirm/custom-confirm.component';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivationsSettlementsExportComponent } from './export/export.component';
import { DefaultParameters } from '@app/shared/models';

@Component({
  selector: 'app-activations-settlements',
  templateUrl: './activations-settlements.component.html',
  styleUrls: ['./activations-settlements.component.scss'],
  providers: [ActivationsSettlementsStore],
})
export class ActivationsSettlementsComponent extends TableExtends {
  @ViewChild(FileUpload) fileUploadEl: FileUpload | null = null;
  viewName = 'activations-settlements';

  isAdmin = false;
  apiLoaded = false;
  data$ = this.store.data$;
  dataIds$ = this.store.dataIds$;
  totalRecords$ = this.store.totalRecords$;
  parameters: DefaultParameters | undefined;
  columns = this.preparedColumns(COLUMNS, 'activationsSettlements.table');

  seperateFilterDates = ['acceptedDeliveryPeriod'];
  filtersWithDateToNextDay = ['acceptedDeliveryPeriod'];
  setRangeFilterDateTime = ['createdDate', 'lastModifiedDate', 'acceptedDeliveryPeriod'];

  get showFilterMenuForAcceptedDeliveryPeriod(): boolean {
    if (!this.viewParameters.dynamicFilters.acceptedDeliveryPeriod) {
      return true;
    }

    const value = this.viewParameters.dynamicFilters.acceptedDeliveryPeriod?.value ?? [];

    return value.filter(Boolean).length < 2;
  }

  constructor(
    injector: Injector,
    route: ActivatedRoute,
    elementRef: ElementRef,
    private store: ActivationsSettlementsStore,
    private service: ActivationsSettlementsService,
    sessionStorage: SessionStorageService,
    private modalService: ModalService,
    private translate: TranslateService,
    protected viewConfigurationService: ViewConfigurationService,
    private toastr: AppToastrService,
    private confirmationService: ConfirmationService,
    private customConfirmComponent: CustomConfirmComponent
  ) {
    super(injector, route, elementRef, sessionStorage, viewConfigurationService);
  }

  getCollection(): void {
    const dateTimeKeys = [
      'createdDate',
      'lastModifiedDate',
      'acceptedDeliveryPeriod',
      'acceptedDeliveryPeriodFrom',
      'acceptedDeliveryPeriodTo',
    ];

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

  getRowClass(row: ActivationsSettlementsDTO): string {
    if (row.activatedVolume) {
      return 'activation-green';
    }
    return 'activation-yellow';
  }

  preview(row: ActivationsSettlementsDTO): void {
    this.modalService.open(ActivationsSettlementsPreviewComponent, { data: row, styleClass: 'full-view' });
  }

  edit(row: ActivationsSettlementsDTO): void {
    const refresh = () => {
      const parameters = this.parameters as DefaultParameters;
      this.store.loadCollection({
        ...parameters,
        refresh: true,
        runAfterGetData: () => this.updateHandyScroll(),
      });
    };
    this.modalService
      .open(ActivationsSettlementsDialogComponent, {
        data: row,
        styleClass: 'full-view',
      })
      .onClose.subscribe(() => refresh());
  }

  formatDeliveryDate(row: ActivationsSettlementsDTO): string {
    return this.service.formatDeliveryDate(row.acceptedDeliveryPeriodFrom, row.acceptedDeliveryPeriodTo);
  }

  showTooltip(row: ActivationsSettlementsDTO): string {
    if (row.acceptedVolumeTooltipVisible) {
      return this.translate.instant('shared.volumeTooltip');
    } else if (row.acceptedVolumeCmvcTooltipVisible) {
      return this.translate.instant('activationsSettlements.table.tooltip.acceptedVolumeCmvcTooltip');
    }
    return '';
  }

  import({ currentFiles }: any): void {
    const formData = new FormData();
    currentFiles.forEach((_: File, index: number) => {
      formData.append('file', currentFiles[index]);
    });

    const importActivationSettlements = (force: boolean = false) =>
      this.service
        .import(formData, force)
        .pipe(
          catchError((response: HttpErrorResponse): any => {
            const errorParams = response.error.params ? JSON.parse(response.error.params) : {};
            const { invalidFiles, invalidActivationSettlements } = errorParams;
            if (!force && response.status === 400 && invalidActivationSettlements?.DUPLICATE_ACTIVATION_SETTLEMENTS) {
              this.confirmationService.confirm({
                message: this.customConfirmComponent.createItemListMessage(
                  this.translate.instant('activationsSettlements.actions.import.reimportQuestion'),
                  invalidActivationSettlements?.DUPLICATE_ACTIVATION_SETTLEMENTS.map(
                    ({ id, companyName, acceptedDeliveryPeriodFrom, acceptedDeliveryPeriodTo }: any) => {
                      return `${id} ${companyName} ${this.service.formatDeliveryDate(
                        acceptedDeliveryPeriodFrom,
                        acceptedDeliveryPeriodTo
                      )}`;
                    }
                  )
                ),
                header: this.translate.instant('activationsSettlements.actions.import.questionHeader'),
                accept: () => importActivationSettlements(true),
                reject: () => this.fileUploadEl?.clear(),
                key: 'confirm-dialog',
              });

              return;
            }

            this.fileUploadEl?.clear();

            if (response.status === 400 && response.error?.errorKey) {
              for (const property in invalidFiles) {
                if (property) {
                  const message = this.translate.instant(property, {
                    files: invalidFiles[property].map((item: string) => `<br>${item.replace(',', '')}`).join(','),
                  });
                  this.toastr.error(message);
                }
              }

              if (invalidActivationSettlements?.SAVING_THE_SAME_ACTIVATION_SETTLEMENTS) {
                const files = invalidActivationSettlements.SAVING_THE_SAME_ACTIVATION_SETTLEMENTS.map(
                  ({ id, companyName, acceptedDeliveryPeriodFrom, acceptedDeliveryPeriodTo }: any) => {
                    return `${id} ${companyName} ${this.service.formatDeliveryDate(acceptedDeliveryPeriodFrom, acceptedDeliveryPeriodTo)}`;
                  }
                ).join(',');
                const message = this.translate.instant(`error.activationSettlements.SAVING_THE_SAME_ACTIVATION_SETTLEMENTS`, {
                  files,
                });
                this.toastr.error(message);
              }

              if (response.error?.errorKey === 'error.activationSettlements.nothingChanged') {
                this.toastr.warning('error.activationSettlements.nothingChanged');
              }
              return;
            }

            this.toastr.error(`activationsSettlements.actions.import.error`);
          })
        )
        .subscribe(() => {
            this.toastr.success(`activationsSettlements.actions.import.success`);
            this.fileUploadEl?.clear();
            this.getCollection();
          },
        );

    importActivationSettlements(false);
  }

  exportData(): void {
    this.modalService.open(ActivationsSettlementsExportComponent, {});
  }
}
