import { Component, ElementRef, Injector, OnInit } from '@angular/core';
import { SessionStorageService, State, getLanguageState } from '@app/core';
import { Observable, distinctUntilChanged, takeUntil } from 'rxjs';
import { Store, select } from '@ngrx/store';

import { ActivatedRoute } from '@angular/router';
import { COLUMNS } from './types.column';
import { Dictionary } from '@app/shared/models';
import { Helpers } from '@app/shared/commons';
import { SchedulingUnitTypeDTO } from './types';
import { SchedulingUnitsTypesService } from './types.service';
import { SchedulingUnitsTypesStore } from './types.store';
import { TableExtends } from '@app/shared/services';
import { TranslateService } from '@ngx-translate/core';
import { ViewConfigurationService } from '@app/shared/commons/view-configuration';

interface Dictionaries {
  products: Observable<Dictionary[]>;
}

@Component({
  selector: 'app-scheduling-units-types',
  templateUrl: './types.component.html',
})
export class SchedulingUnitsTypesComponent extends TableExtends implements OnInit {
  viewName = 'scheduling-unit-types';

  dictionaries: Dictionaries = {
    products: this.service.getProducts(),
  };

  data$ = this.store.data$;
  dataIds$ = this.store.dataIds$;
  totalRecords$ = this.store.totalRecords$;
  columns = this.preparedColumns(COLUMNS, 'schedulingUnits.table');

  constructor(
    injector: Injector,
    route: ActivatedRoute,
    elementRef: ElementRef,
    private appStore: Store<State>,
    sessionStorage: SessionStorageService,
    private translate: TranslateService,
    private store: SchedulingUnitsTypesStore,
    private service: SchedulingUnitsTypesService,
    protected viewConfigurationService: ViewConfigurationService
  ) {
    super(injector, route, elementRef, sessionStorage, viewConfigurationService);

    this.preparedColumnsToView();
  }

  ngOnInit(): void {
    this.loadViewConfiguration();

    this.getCollection();
    this.subscribeDynamicFilters();

    this.subscribeQueryParams();

    this.appStore
      .pipe(select(getLanguageState), distinctUntilChanged(), takeUntil(this.destroy$))
      .subscribe(() => this.preparedColumnsToView());
  }

  getCollection(): void {
    this.store.loadCollection({
      page: this.page,
      size: this.rows,
      sort: this.sort,
      runAfterGetData: () => this.updateHandyScroll(),
      filters: Helpers.serializeFilters(this.staticFilters, this.dynamicFilters, ['createdDate', 'lastModifiedDate']),
    });
  }

  getProductValues(row: SchedulingUnitTypeDTO): string {
    const products = row.products ?? [];

    return products.map(({ shortName }) => shortName).join(', ');
  }

  private preparedColumnsToView(): void {
    this.columns = this.preparedColumns(COLUMNS, 'schedulingUnits.table').filter(({ field }) => {
      if (this.translate.currentLang === 'pl') {
        return !['descriptionEn'].includes(field);
      }

      return !['descriptionPl'].includes(field);
    });

    this.onActiveColumnsChange(false);
  }
}
