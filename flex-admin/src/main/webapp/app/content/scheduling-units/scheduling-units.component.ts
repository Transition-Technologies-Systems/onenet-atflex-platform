import { AppToastrService, AuthService, SessionStorageService } from '@app/core';
import { Component, ElementRef, Injector, OnInit } from '@angular/core';
import { ConfirmationService, MenuItem } from 'primeng/api';
import { Helpers, ModalService } from '@app/shared/commons';
import { SchedulingUnitDTO, UnitMinDTO } from './scheduling-units';
import { SchedulingUnitsStore } from './scheduling-units.store';

import { ActivatedRoute } from '@angular/router';
import { COLUMNS } from './scheduling-units.columns';
import { DefaultParameters, Dictionary } from '@app/shared/models';
import { DirectionOfDeviationType } from '@app/shared/enums';
import { HttpErrorResponse } from '@angular/common/http';
import { ProposalComponent } from '@app/shared/proposal';
import { ROLES_WITH_FSP } from '@app/app.config';
import { SchedulingUnitsDialogComponent } from './dialog';
import { SchedulingUnitsPreviewComponent } from './preview/preview.component';
import { SchedulingUnitsService } from './scheduling-units.service';
import { TableExtends } from '@app/shared/services';
import { TranslateService } from '@ngx-translate/core';
import { ViewConfigurationService } from '@app/shared/commons/view-configuration';
import { takeUntil, catchError } from 'rxjs/operators';

interface Dictionaries {
  directions: Dictionary[];
}

@Component({
  selector: 'app-scheduling-unit',
  templateUrl: './scheduling-units.component.html',
  styleUrls: ['./scheduling-units.component.scss'],
  providers: [ConfirmationService],
})
export class SchedulingUnitsComponent extends TableExtends implements OnInit {
  viewName = 'scheduling-unit';

  isRegister = false;
  hasAdminRole = false;
  data$ = this.store.data$;
  dataIds$ = this.store.dataIds$;
  mangeRoles = ROLES_WITH_FSP;
  totalRecords$ = this.store.totalRecords$;
  parameters: DefaultParameters | undefined;
  columns = this.preparedColumns(COLUMNS, 'schedulingUnits.table');

  dictionaries: Dictionaries = {
    directions: Helpers.enumToDictionary(DirectionOfDeviationType, 'DirectionOfDeviationType'),
  };

  exportOptions = (): MenuItem[] => [
    {
      label: this.translate.instant('schedulingUnits.actions.export.allData'),
      icon: 'pi pi-fw pi-download',
      command: () => this.export(true),
    },
    {
      label: this.translate.instant('schedulingUnits.actions.export.displayedData'),
      icon: 'pi pi-fw pi-download',
      command: () => this.export(),
    },
  ];

  private schedulungUnitId: number | undefined;

  constructor(
    injector: Injector,
    route: ActivatedRoute,
    elementRef: ElementRef,
    private authService: AuthService,
    private store: SchedulingUnitsStore,
    private service: SchedulingUnitsService,
    private toastr: AppToastrService,
    sessionStorage: SessionStorageService,
    private modalService: ModalService,
    private translate: TranslateService,
    private confirmationService: ConfirmationService,
    protected viewConfigurationService: ViewConfigurationService
  ) {
    super(injector, route, elementRef, sessionStorage, viewConfigurationService);

    this.isRegister = route.snapshot.data?.type === 'REGISTER';
    this.schedulungUnitId = route.snapshot.queryParams?.id ?? null;

    this.columns = this.preparedColumns(COLUMNS, 'schedulingUnits.table').filter(({ field }) => {
      if (this.isRegister) {
        return !['active', 'readyForTests', 'certified', 'numberOfDers', 'delete'].includes(field);
      } else {
        return !['units', 'createdBy', 'lastModifiedBy'].includes(field);
      }
    });

    this.onActiveColumnsChange(false);

    this.authService.hasRole('ROLE_ADMIN').then((hasRole: boolean) => {
      this.hasAdminRole = hasRole;
    });
  }

  ngOnInit(): void {
    this.loadViewConfiguration();

    this.getCollection();
    this.subscribeDynamicFilters();

    this.subscribeQueryParams();

    if (this.schedulungUnitId) {
      this.viewParameters.dynamicFilters = {
        ...this.viewParameters.dynamicFilters,
        id: { matchMode: 'equals', value: this.schedulungUnitId },
      };
    }
  }

  add(): void {
    const dialog = this.modalService.open(SchedulingUnitsDialogComponent, { styleClass: 'full-view' });

    dialog.onClose.pipe(takeUntil(this.destroy$)).subscribe((result: boolean) => {
      if (!!result) {
        this.getCollection();
      }
    });
  }

  delete(event: Event, row: SchedulingUnitDTO): void {
    this.confirmationService.confirm({
      target: event.target || undefined,
      message: this.translate.instant('schedulingUnits.actions.delete.question'),
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.service
          .remove(row.id)
          .pipe(
            catchError((response: HttpErrorResponse): any => {
              if (!(response.status === 400 && response.error?.errorKey)) {
                this.toastr.error('schedulingUnits.actions.delete.error');
                return;
              }
            })
          )
          .subscribe(() => {
            this.toastr.success('schedulingUnits.actions.delete.success');
            this.getCollection();
          });
      },
    });
  }

  edit(row: SchedulingUnitDTO): void {
    const dialog = this.modalService.open(SchedulingUnitsDialogComponent, { data: row, styleClass: 'full-view' });

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
      filters: Helpers.serializeFilters(this.staticFilters, this.dynamicFilters, dateTimeKeys),
    };

    if (this.isRegister) {
      this.parameters = {
        ...this.parameters,
        filters: {
          ...this.parameters.filters,
          'certified.equals': true,
        },
      };
    }

    this.store.loadCollection({
      ...this.parameters,
      runAfterGetData: () => this.updateHandyScroll(),
    });
  }

  getCouplingPoints(row: SchedulingUnitDTO): string {
    return row.couplingPoints?.map(({ name }) => name).join(', ');
  }

  getUnits(units: UnitMinDTO[] | undefined): string {
    if (!units) {
      return '';
    }

    return units.map(({ name, sourcePower }) => `${name}(${sourcePower} kW)`).join(', ');
  }

  preview(row: SchedulingUnitDTO): void {
    this.modalService
      .open(SchedulingUnitsPreviewComponent, { data: { ...row, isRegister: this.isRegister }, styleClass: 'full-view' })
      .onClose.subscribe((refreshData: boolean) => {
        if (!!refreshData) {
          const parameters = this.parameters as DefaultParameters;

          this.store.loadCollection({
            ...parameters,
            runAfterGetData: () => this.updateHandyScroll(),
          });
        }
      });
  }

  proposal(row: SchedulingUnitDTO): void {
    this.modalService.open(ProposalComponent, {
      data: {
        schedulingUnit: row,
      },
    });
  }

  private export(allData: boolean = false): void {
    this.service.exportXLSX(this.parameters, allData, this.isRegister);
  }
}
