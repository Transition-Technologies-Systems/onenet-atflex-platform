import { AppToastrService, AuthService, SessionStorageService, ToastrMessage } from '@app/core';
import { BooleanEnum, DirectionOfDeviationType, Role } from '@app/shared/enums';
import { Component, ElementRef, Injector, OnInit } from '@angular/core';
import { ConfirmationService, MenuItem } from 'primeng/api';
import { Dictionary, FspDTO } from '@app/shared/models';
import { FspsParameters, FspsStore } from './fsps.store';
import { Helpers, ModalService } from '@app/shared/commons';

import { ActivatedRoute } from '@angular/router';
import { COLUMNS } from './fsps.columns';
import { FspsDialogComponent } from './dialog';
import { FspsService } from './fsps.service';
import { HttpErrorResponse } from '@angular/common/http';
import { ProposalComponent } from '@app/shared/proposal';
import { TableExtends } from '@app/shared/services';
import { TranslateService } from '@ngx-translate/core';
import { ViewConfigurationService } from '@app/shared/commons/view-configuration';
import { takeUntil, catchError } from 'rxjs/operators';

interface Dictionaries {
  roles: Dictionary[];
  boolean: Dictionary[];
  directions: Dictionary[];
}

@Component({
  selector: 'app-fsps',
  templateUrl: './fsps.component.html',
  providers: [ConfirmationService],
})
export class FspsComponent extends TableExtends implements OnInit {
  viewName = 'fsps';
  isBsp = false;

  hasAdminRole = false;
  data$ = this.store.data$;
  dataIds$ = this.store.dataIds$;
  totalRecords$ = this.store.totalRecords$;
  parameters: FspsParameters | undefined;
  columns = this.preparedColumns(COLUMNS, 'fsps.table');

  seperateFilterDates = ['valid'];
  setRangeFilterDateTime = ['createdDate', 'lastModifiedDate', 'valid'];

  dictionaries: Dictionaries = {
    roles: Helpers.enumToDictionary<Role>(Role, 'Role').filter(({ value }) =>
      [Role.ROLE_FLEX_SERVICE_PROVIDER, Role.ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED].includes(value)
    ),
    boolean: Helpers.enumToDictionary(BooleanEnum, 'Boolean'),
    directions: Helpers.enumToDictionary(DirectionOfDeviationType, 'DirectionOfDeviationType'),
  };

  get roleName(): string {
    const role = this.isBsp ? 'ROLE_BALANCING_SERVICE_PROVIDER' : 'ROLE_FLEX_SERVICE_PROVIDER';

    return this.translate.instant(`RoleShort.${role}`);
  }

  exportOptions = (): MenuItem[] => [
    {
      label: this.translate.instant('fsps.actions.export.allData', { role: this.roleName }),
      icon: 'pi pi-fw pi-download',
      command: () => this.export(true),
    },
    {
      label: this.translate.instant('fsps.actions.export.displayedData', { role: this.roleName }),
      icon: 'pi pi-fw pi-download',
      command: () => this.export(),
    },
  ];

  constructor(
    injector: Injector,
    route: ActivatedRoute,
    elementRef: ElementRef,
    private store: FspsStore,
    private service: FspsService,
    private authService: AuthService,
    private toastr: AppToastrService,
    sessionStorage: SessionStorageService,
    private modalService: ModalService,
    private translate: TranslateService,
    private confirmationService: ConfirmationService,
    protected viewConfigurationService: ViewConfigurationService
  ) {
    super(injector, route, elementRef, sessionStorage, viewConfigurationService);

    this.isBsp = route.snapshot.data?.role === Role.ROLE_BALANCING_SERVICE_PROVIDER;

    this.columns = this.preparedColumns(COLUMNS, 'fsps.table').filter(({ field }) =>
      this.isBsp ? field !== 'role' : field !== 'agreementWithTso'
    );

    this.onActiveColumnsChange(false);

    this.authService.hasRole('ROLE_ADMIN').then((hasRole: boolean) => {
      this.hasAdminRole = hasRole;
    });
  }

  delete(event: Event, row: FspDTO): void {
    const role = this.isBsp ? 'ROLE_BALANCING_SERVICE_PROVIDER' : 'ROLE_FLEX_SERVICE_PROVIDER';
    this.confirmationService.confirm({
      target: event.target || undefined,
      message: this.translate.instant('fsps.actions.delete.question', {
        role: this.translate.instant(`RoleShort.${role}`),
      }),
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.service
          .remove(row.id)
          .pipe(
            catchError((response: HttpErrorResponse): any => {
              if (!(response.status === 400 && response.error?.errorKey)) {
                this.toastr.error(new ToastrMessage({ msg: 'fsps.actions.delete.error', params: { role } }));
                return;
              }
            })
          )
          .subscribe(() => {
            this.toastr.success(new ToastrMessage({ msg: 'fsps.actions.delete.success', params: { role } }));
            this.getCollection();
          });
      },
    });
  }

  edit(row: FspDTO): void {
    const dialog = this.modalService.open(FspsDialogComponent, {
      data: {
        model: row,
        isBsp: this.isBsp,
        roleName: this.roleName,
      },
      styleClass: 'full-view',
    });

    dialog.onClose.pipe(takeUntil(this.destroy$)).subscribe((result: boolean) => {
      if (!!result) {
        this.getCollection();
      }
    });
  }

  getCollection(): void {
    const dateTimeKeys = ['createdDate', 'lastModifiedDate', 'validFrom', 'validTo'];

    this.parameters = {
      page: this.page,
      size: this.rows,
      sort: this.sort,
      bsp: this.isBsp,
      filters: Helpers.serializeFilters(this.staticFilters, this.dynamicFilters, dateTimeKeys),
    };

    this.store.loadCollection({
      ...this.parameters,
      runAfterGetData: () => this.updateHandyScroll(),
    });
  }

  proposal(row: FspDTO): void {
    this.modalService.open(ProposalComponent, {
      data: {
        bsp: row,
      },
    });
  }

  private export(allData: boolean = false): void {
    this.service.exportXLSX(this.parameters, allData);
  }
}
