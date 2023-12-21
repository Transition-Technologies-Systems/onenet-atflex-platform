import { SessionStorageService } from '@app/core';
import { BooleanEnum, DirectionOfDeviationType, Role } from '@app/shared/enums';
import { Component, ElementRef, Injector, OnInit } from '@angular/core';
import { MenuItem } from 'primeng/api';
import { Dictionary, FspDTO } from '@app/shared/models';
import { FspsParameters, FspsStore } from './fsps.store';
import { Helpers, ModalService } from '@app/shared/commons';

import { ActivatedRoute } from '@angular/router';
import { COLUMNS } from './fsps.columns';
import { FspsService } from './fsps.service';
import { ProposalComponent } from '@app/shared/proposal';
import { TableExtends } from '@app/shared/services';
import { TranslateService } from '@ngx-translate/core';
import { ViewConfigurationService } from '@app/shared/commons/view-configuration';

interface Dictionaries {
  roles: Dictionary[];
  boolean: Dictionary[];
  directions: Dictionary[];
}

@Component({
  selector: 'app-fsps',
  templateUrl: './fsps.component.html',
  styleUrls: ['./fsps.component.scss'],
})
export class FspsComponent extends TableExtends implements OnInit {
  viewName = 'fsps';
  isBsp = false;

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
    sessionStorage: SessionStorageService,
    private modalService: ModalService,
    private translate: TranslateService,
    protected viewConfigurationService: ViewConfigurationService
  ) {
    super(injector, route, elementRef, sessionStorage, viewConfigurationService);

    this.isBsp = route.snapshot.data?.role === Role.ROLE_BALANCING_SERVICE_PROVIDER || true;

    this.columns = this.preparedColumns(COLUMNS, 'fsps.table').filter(({ field }) =>
      this.isBsp ? field !== 'role' : field !== 'agreementWithTso'
    );

    this.onActiveColumnsChange(false);
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
