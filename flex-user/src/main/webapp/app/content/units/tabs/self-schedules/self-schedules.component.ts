import { catchError } from 'rxjs/operators';
import * as moment from 'moment';

import { AppToastrService, AuthService, SessionStorageService } from '@app/core';
import { Component, ElementRef, Injector, OnInit, ViewChild } from '@angular/core';
import { Helpers, ModalService } from '@app/shared/commons';
import { UnitsSelfScheduleStore } from './self-schedules.store';

import { ActivatedRoute } from '@angular/router';
import { COLUMNS } from './self-schedules.columns';
import { ConfirmationService } from 'primeng/api';
import { FileUpload } from 'primeng/fileupload';
import { HttpErrorResponse } from '@angular/common/http';
import { SelfScheduleFileDTO } from './self-schedule';
import { TableExtends } from '@app/shared/services';
import { TranslateService } from '@ngx-translate/core';
import { UnitsSelfSchedulePreviewComponent } from './preview';
import { UnitsSelfSchedulesService } from './self-schedules.service';
import { ViewConfigurationService } from '@app/shared/commons/view-configuration';
import { CustomConfirmComponent } from '@app/shared/commons/custom-confirm/custom-confirm.component';
import { DefaultParameters } from '@app/shared/models';

@Component({
  selector: 'app-units-self-schedules',
  templateUrl: './self-schedules.component.html',
  providers: [ConfirmationService, UnitsSelfScheduleStore],
})
export class UnitsSelfSchedulesComponent extends TableExtends implements OnInit {
  @ViewChild(FileUpload) fileUploadEl: FileUpload | null = null;

  viewName = 'units-self-schedule';

  isAdmin = false;
  data$ = this.store.data$;
  dataIds$ = this.store.dataIds$;
  totalRecords$ = this.store.totalRecords$;
  parameters: DefaultParameters | undefined;
  columns = this.preparedColumns(COLUMNS, 'units.selfSchedule.table');

  constructor(
    injector: Injector,
    route: ActivatedRoute,
    elementRef: ElementRef,
    private authService: AuthService,
    private toastr: AppToastrService,
    sessionStorage: SessionStorageService,
    private modalService: ModalService,
    private translate: TranslateService,
    private store: UnitsSelfScheduleStore,
    private service: UnitsSelfSchedulesService,
    private confirmationService: ConfirmationService,
    private customConfirmComponent: CustomConfirmComponent,
    protected viewConfigurationService: ViewConfigurationService
  ) {
    super(injector, route, elementRef, sessionStorage, viewConfigurationService);

    this.checkRole();
  }

  delete(event: Event, row: SelfScheduleFileDTO): void {
    this.confirmationService.confirm({
      target: event.target || undefined,
      message: this.translate.instant('units.actions.selfSchedule.delete.question'),
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.service
          .remove(row.id)
          .pipe(
            catchError((response: HttpErrorResponse): any => {
              if (!(response.status === 400 && response.error?.errorKey)) {
                this.toastr.error('units.actions.selfSchedule.delete.error');
                return;
              }
            })
          )
          .subscribe(() => {
            this.toastr.success('units.actions.selfSchedule.delete.success');
            this.getCollection();
          });
      },
    });
  }

  download(): void {
    this.service.downloadTemplate();
  }

  getCollection(): void {
    const dateTimeKeys = ['createdDate', 'lastModifiedDate', 'selfScheduleDate'];

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

  import({ currentFiles }: any): void {
    const formData = new FormData();
    currentFiles.forEach((item: File, index: number) => {
      formData.append('file', currentFiles[index]);
    });

    const importSelfSchedule = (force: boolean = false) =>
      this.service
        .import(formData, force)
        .pipe(
          catchError((response: HttpErrorResponse): any => {
            const errorParams = response.error.params ? JSON.parse(response.error.params) : {};
            const { invalidFiles, invalidSelfSchedule } = errorParams;
            if (!force && response.status === 400 && invalidSelfSchedule?.DUPLICATE_SELF_SCHEDULE) {
              this.confirmationService.confirm({
                message: this.customConfirmComponent.createItemListMessage(
                  this.translate.instant('units.actions.selfSchedule.import.reimportQuestion'),
                  invalidSelfSchedule?.DUPLICATE_SELF_SCHEDULE.map(({ fspName, unitName, selfScheduleDate }: any) => {
                    return `${fspName} ${unitName} ${moment(selfScheduleDate).format('DD/MM/YYYY')}`;
                  })
                ),
                header: this.translate.instant('units.actions.selfSchedule.import.questionHeader'),
                accept: () => importSelfSchedule(true),
                reject: () => this.fileUploadEl?.clear(),
                key: 'confirm-dialog',
              });

              return;
            }

            this.fileUploadEl?.clear();

            if (response.status === 400 && response.error?.errorKey) {
              for (const property in invalidFiles) {
                if (property) {
                  const message = this.translate.instant(property, { files: invalidFiles[property] });
                  this.toastr.error(message);
                }
              }

              if (invalidSelfSchedule?.SAVING_THE_SAME_SELF_SCHEDULES) {
                const files = invalidSelfSchedule.SAVING_THE_SAME_SELF_SCHEDULES.map(({ fspName, unitName, selfScheduleDate }: any) => {
                  return `${fspName} ${unitName} ${moment(selfScheduleDate).format('DD/MM/YYYY')}`;
                }).join(', ');
                const message = this.translate.instant(`error.selfSchedule.SAVING_THE_SAME_SELF_SCHEDULES`, {
                  files,
                });
                this.toastr.error(message);
              }

              const errorsWithStringifyParams = [
                'error.selfSchedule.derExceedsTechnicalLimits',
                'error.selfSchedule.dersExceedTechnicalLimits',
              ];

              if (errorsWithStringifyParams.includes(response.error?.errorKey)) {
                const params = JSON.parse(response.error?.params);
                const message = this.translate.instant(response.error?.errorKey, { ...params });
                this.toastr.error(message);
              }
              return;
            }

            this.toastr.error(`units.actions.selfSchedule.import.error`);
          })
        )
        .subscribe(() => {
          this.toastr.success(`units.actions.selfSchedule.import.success`);
          this.fileUploadEl?.clear();
          this.getCollection();
        });

    importSelfSchedule(false);
  }

  preview(row: SelfScheduleFileDTO): void {
    this.modalService.open(UnitsSelfSchedulePreviewComponent, { data: row, styleClass: 'full-view' });
  }

  private checkRole(): void {
    this.authService.hasRole('ROLE_ADMIN').then((hasRole: boolean) => {
      this.isAdmin = hasRole;
    });
  }
}
