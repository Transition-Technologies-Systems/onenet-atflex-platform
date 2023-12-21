import { catchError } from 'rxjs';
import { AppToastrService, AuthService, SessionStorageService } from '@app/core';
import { Component, ElementRef, Injector } from '@angular/core';
import { Helpers, ModalService } from '@app/shared/commons';
import { PartnershipDTO, Tab, TabType } from './partnership';

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

  data$ = this.store.data$;
  totalRecords$ = this.store.totalRecords$;
  columns = this.preparedColumns(COLUMNS, 'partnership.table');

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
    this.changeTab();
  }

  cancel(event: Event, row: PartnershipDTO): void {
    this.confirmationService.confirm({
      target: event.target || undefined,
      message: this.translate.instant('partnership.actions.cancel.question'),
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.service
          .cancel(row.id)
          .pipe(
            catchError((response: HttpErrorResponse): any => {
              if (!(response.status === 400 && response.error?.errorKey)) {
                this.toastr.error('partnership.actions.resend.error');
                return;
              }
            })
          )
          .subscribe(() => {
            this.toastr.success('partnership.actions.cancel.success');
            this.getCollection();
          });
      },
    });
  }

  changeTab(): void {
    this.viewParameters.dynamicFilters = {
      bspName: { value: null, matchMode: 'contains' },
      fspName: { value: null, matchMode: 'contains' },
      ...this.viewParameters.dynamicFilters,
    };

    this.getCollection();
  }

  getCollection(): void {
    this.store.loadCollection({
      page: this.page,
      size: this.rows,
      sort: this.sort,
      tabType: this.selectedTab,
      filters: Helpers.serializeFilters(this.staticFilters, this.dynamicFilters, ['sentDate']),
    });
  }

  getTabs(): Tab[] {
    return this.service.getTabs();
  }

  preview(row: PartnershipDTO): void {
    this.modalService.open(ProposalConfirmComponent, {
      baseZIndex: 2000,
      data: {
        id: row.id,
        type: this.selectedTab === 'INVITATION' ? 'BSP' : 'FSP',
      },
    });
  }

  resend(event: Event, row: PartnershipDTO): void {
    this.confirmationService.confirm({
      target: event.target || undefined,
      message: this.translate.instant('partnership.actions.resend.question'),
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.service
          .resend(row, this.selectedTab)
          .pipe(
            catchError((response: HttpErrorResponse): any => {
              if (!(response.status === 400 && response.error?.errorKey)) {
                this.toastr.error('partnership.actions.resend.error');
                return;
              }
            })
          )
          .subscribe(() => {
            this.toastr.success('partnership.actions.resend.success');
            this.getCollection();
          });
      },
    });
  }

  private checkRole(): void {
    this.authService.hasAnyRoles([Role.ROLE_ADMIN]).then((isAdmin: boolean) => {
      if (!isAdmin) {
        this.columns = this.preparedColumns(COLUMNS, 'partnership.table').filter(({ field }) => field !== 'actions');
        this.onActiveColumnsChange(false);
      }
    });
  }
}
