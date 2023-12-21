import { Observable, tap, } from 'rxjs';
import { Component, Injector, ElementRef, AfterViewInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { AuthService, SessionStorageService } from '@app/core';
import { Helpers } from '@app/shared/commons';
import { ViewConfigurationService } from '@app/shared/commons/view-configuration';
import { DefaultParameters, Dictionary } from '@app/shared/models';
import { KpiDTO } from '@app/shared/models/kpi';
import { TableExtends } from '@app/shared/services';
import * as moment from 'moment';
import { COLUMNS } from './kpi.columns';
import { KpiService } from './kpi.service';
import { KpiStore } from './kpi.store';

interface Dictionaries {
  kpi: Observable<Dictionary[]>;
}

@Component({
  selector: 'app-kpi',
  templateUrl: './kpi.component.html',
})
export class KpiComponent extends TableExtends implements AfterViewInit {
  viewName = 'kpi';

  data$ = this.store.data$;
  dataIds$ = this.store.dataIds$;
  totalRecords$ = this.store.totalRecords$;
  parameters: DefaultParameters | undefined;
  columns = this.preparedColumns(COLUMNS, 'kpi.table');
  seperateFilterDates = ['date'];
  setRangeFilterDateTime = ['date'];
  shouldCreateKpi = false;

  dictionaries: Dictionaries = {
    kpi: this.service.getKpiTypes(),
  };

  get showFilterMenuForDate(): boolean {
    if (!this.viewParameters.dynamicFilters.date) {
      return true;
    }

    const value = this.viewParameters.dynamicFilters.date?.value ?? [];

    return value.filter(Boolean).length < 2;
  }

  constructor(
    injector: Injector,
    route: ActivatedRoute,
    elementRef: ElementRef,
    private store: KpiStore,
    private service: KpiService,
    sessionStorage: SessionStorageService,
    protected viewConfigurationService: ViewConfigurationService,
    private authService: AuthService,
  ) {
    super(injector, route, elementRef, sessionStorage, viewConfigurationService);

    this.onActiveColumnsChange(false);
  }

  ngAfterViewInit(): void {
    this.afterViewInit();
    this.authService.hasAuthority('FLEX_ADMIN_KPI_MANAGE').then(hasAuthority => {
      this.shouldCreateKpi = hasAuthority;
    });
  }

  getCollection(): void {
    const dateTimeKeys = ['createdDate', 'date', 'dateFrom', 'dateTo'];

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

  generate(value: any): void {
    const formValue = {
      type: value.type,
      dateFrom: value.date.from ? moment(value.date.from).toISOString() : undefined,
      dateTo: value.date.to ? moment(value.date.to).add(1, 'd').toISOString() : undefined,
    };

    this.service
      .generateKpi(formValue)
      .pipe(
        tap(() => {
          this.getCollection();
        }),
      )
      .subscribe();
  }

  regenerate(row: KpiDTO): void {
    this.service.regenerateKpi(row.id);
  }

  displayDateRange(dateFrom: string, dateTo: string): string {
    if (dateFrom && dateTo) {
      return `${moment(dateFrom).format('DD/MM/yyyy')} - ${moment(dateTo).subtract(1, 'd').format('DD/MM/yyyy')}`;
    }
    return '';
  }
}
