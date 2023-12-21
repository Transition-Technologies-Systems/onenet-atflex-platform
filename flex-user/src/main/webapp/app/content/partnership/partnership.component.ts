import { AppToastrService, AuthService, SessionStorageService } from '@app/core';
import { Component, ElementRef, Injector } from '@angular/core';
import { Helpers, ModalService } from '@app/shared/commons';
import { PartnershipDTO, Tab, TabType } from './partnership';
import { takeUntil, catchError } from 'rxjs/operators';
import { ActivatedRoute } from '@angular/router';
import { COLUMNS } from './partnership.columns';
import { ConfirmationService } from 'primeng/api';
import { HttpErrorResponse } from '@angular/common/http';
import { PartnershipService } from './partnership.service';
import { PartnershipStore } from './partnership.store';
import { ProposalConfirmComponent } from '@app/shared/proposal/confirm';
import { Role } from '@app/shared/enums';
import { TableExtends } from '@app/shared/services';
import { TranslateService } from '@ngx-translate/core';
import { ViewConfigurationService } from '@app/shared/commons/view-configuration';

@Component({
  selector: 'app-partnership',
  templateUrl: './partnership.component.html',
  providers: [ConfirmationService, PartnershipStore],
})
export class PartnershipComponent extends TableExtends {
  viewName = 'partnership';
  selectedTab: TabType = 'INVITATION';
  sort = ['statusSortOrder,asc', 'sentDate,desc'];

  isFsp = false;
  isBsp = false;

  data$ = this.store.data$;
  totalRecords$ = this.store.totalRecords$;
  columns = this.preparedColumns(COLUMNS, 'partnership.table');

  get showAction(): boolean {
    if (this.selectedTab === 'INVITATION') {
      return this.isBsp;
    }

    return this.isFsp;
  }

  get showPreview(): boolean {
    if (this.selectedTab === 'INVITATION') {
      return this.isFsp;
    }

    return this.isBsp;
  }

  constructor(
    injector: Injector,
    route: ActivatedRoute,
    elementRef: ElementRef<any>,
    private store: PartnershipStore,
    private toastr: AppToastrService,
    private authService: AuthService,
    sessionStorage: SessionStorageService,
    private modalService: ModalService,
    private service: PartnershipService,
    private translate: TranslateService,
    private confirmationService: ConfirmationService,
    viewConfigurationService: ViewConfigurationService
  ) {
    super(injector, route, elementRef, sessionStorage, viewConfigurationService);

    this.checkRole();
  }

  cancel(event: Event, row: PartnershipDTO): void {
    this.confirmationService.confirm({
      target: event.target || undefined,
      message: this.translate.instant(`partnership.actions.cancel.${this.isBsp ? 'questionBsp' : 'question'}`),
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.service
          .cancel(row.id)
          .pipe(
            catchError((response: HttpErrorResponse): any => {
              if (!(response.status === 400 && response.error?.errorKey)) {
                this.toastr.error(`partnership.actions.resend.${this.isBsp ? 'errorBsp' : 'error'}`);
                return;
              }
            })
          )
          .subscribe(() => {
            this.toastr.success(`partnership.actions.cancel.${this.isBsp ? 'successBsp' : 'success'}`);
            this.getCollection();
          });
      },
    });
  }

  changeTab(): void {
    this.columns = this.preparedColumns(COLUMNS, 'partnership.table').filter(({ field }) => {
      const excludeKeys = [];

      if (this.isFsp) {
        excludeKeys.push(this.selectedTab === 'INVITATION' ? 'sentDate' : 'lastModifiedDate');
      } else if (this.isBsp) {
        excludeKeys.push(this.selectedTab === 'REQUEST' ? 'sentDate' : 'lastModifiedDate');
      }

      return !excludeKeys.includes(field);
    });

    this.viewParameters.dynamicFilters = {
      bspName: { value: null, matchMode: 'contains' },
      fspName: { value: null, matchMode: 'contains' },
      ...this.viewParameters.dynamicFilters,
    };

    this.onActiveColumnsChange(false);

    this.getCollection();
  }

  getCollection(): void {
    this.store.loadCollection({
      page: this.page,
      size: this.rows,
      sort: this.sort,
      tabType: this.selectedTab,
      filters: Helpers.serializeFilters(this.staticFilters, this.dynamicFilters, ['sentDate', 'lastModifiedDate']),
    });
  }

  getTabs(): Tab[] {
    return this.service.getTabs(this.isBsp);
  }

  preview(row: PartnershipDTO): void {
    const dialog = this.modalService.open(ProposalConfirmComponent, {
      baseZIndex: 2000,
      data: {
        id: row.id,
        type: this.selectedTab === 'INVITATION' ? 'BSP' : 'FSP',
      },
    });

    dialog.onClose.pipe(takeUntil(this.destroy$)).subscribe((result: boolean) => {
      if (!!result) {
        this.getCollection();
      }
    });
  }

  resend(event: Event, row: PartnershipDTO): void {
    this.confirmationService.confirm({
      target: event.target || undefined,
      message: this.translate.instant(`partnership.actions.resend.${this.isBsp ? 'questionBsp' : 'question'}`),
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.service
          .resend(row, this.selectedTab)
          .pipe(
            catchError((response: HttpErrorResponse): any => {
              if (!(response.status === 400 && response.error?.errorKey)) {
                this.toastr.error(`partnership.actions.resend.${this.isBsp ? 'errorBsp' : 'error'}`);
                return;
              }
            })
          )
          .subscribe(() => {
            this.toastr.success(`partnership.actions.resend.${this.isBsp ? 'successBsp' : 'success'}`);
            this.getCollection();
          });
      },
    });
  }

  private checkRole(): void {
    this.authService.hasAnyRoles([Role.ROLE_FLEX_SERVICE_PROVIDER, Role.ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED]).then((isFsp: boolean) => {
      this.isFsp = isFsp;

      if (isFsp) {
        this.selectedTab = 'INVITATION';
        this.columns = this.preparedColumns(COLUMNS, 'partnership.table').filter(({ field }) => field !== 'sentDate');

        this.onActiveColumnsChange(false);
      }
    });

    this.authService.hasAnyRoles([Role.ROLE_BALANCING_SERVICE_PROVIDER]).then((isBsp: boolean) => {
      this.isBsp = isBsp;

      if (isBsp) {
        this.selectedTab = 'REQUEST';
        this.columns = this.preparedColumns(COLUMNS, 'partnership.table').filter(({ field }) => field !== 'sentDate');

        this.onActiveColumnsChange(false);
      }
    });
  }
}
