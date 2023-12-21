import * as moment from 'moment';

import { AfterViewInit, Component, ElementRef, Injector, NgZone, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { BoundingBox, Column } from '../models';
import { UserScreenConfigDTO, ViewConfigurationService } from '../commons/view-configuration';
import { debounceTime, takeUntil } from 'rxjs/operators';

import { ActivatedRoute } from '@angular/router';
import { FilterMatchMode } from 'primeng/api';
import { Helpers } from '../commons';
import { SessionStorageService } from '@app/core';
import { Screen } from '../enums';
import { Subject } from 'rxjs';
import { Table } from 'primeng/table';
import handyScroll from 'handy-scroll';
import { isEqual } from 'lodash';
import { isNil } from 'lodash-es';

const DATE_REGEX = /\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}.\d{3}Z/gm;

type GetCollectionType = 'filters' | 'sort';

export interface TableExtendsParameters {
  runAfterGetData?: () => void;
  refresh?: boolean;
}

@Component({
  template: '',
})
// tslint:disable-next-line:component-class-suffix
export abstract class TableExtends implements OnInit, OnDestroy, AfterViewInit {
  setRangeFilterDateTime: string[] = ['createdDate', 'lastModifiedDate'];
  filtersWithDateToNextDay: string[] = [];
  seperateFilterDates: string[] = [];
  filtersWithTime: string[] = [];

  columns: Column[] = [];
  activeColumns: string[] = [];
  columnsState: { [field: string]: boolean } = {};

  sectionBoudingClientRect: BoundingBox | undefined;
  filtersChange$ = new Subject<object>();
  sort: string | string[] = 'id,desc';
  disabledHandyScroll = false;
  screen: Screen | undefined;
  dynamicFilters: any = {};
  staticFilters: any = {};
  inModal = false;
  rows = 25;
  page = 0;

  viewParameters: { [type: string]: any } = {
    dynamicFilters: {},
    staticFilters: {},
  };

  @ViewChild(Table, { static: false }) table: Table | undefined;

  get wrapperHeight(): number {
    return this.sectionBoudingClientRect?.height || 250;
  }

  protected ngZone: NgZone;
  protected destroy$ = new Subject<void>();
  protected tableWrapperViewChild: ElementRef | undefined;
  protected viewConfiguration: UserScreenConfigDTO | undefined;
  private resizeObserver = new ResizeObserver(() =>
    this.ngZone.runOutsideAngular(() => setTimeout(() => this.getSectionBoudingClientRect(true)))
  );

  abstract viewName: string | undefined;
  abstract getCollection(type?: GetCollectionType): void;

  constructor(
    public injector: Injector,
    public route: ActivatedRoute,
    public elementRef: ElementRef,
    public sessionStorage: SessionStorageService,
    protected viewConfigurationService: ViewConfigurationService
  ) {
    this.ngZone = injector.get(NgZone);
    this.screen = route.snapshot.data?.screen;
    this.viewConfiguration = route.snapshot.data?.viewConfiguration;
  }

  ngOnInit(): void {
    this.loadViewConfiguration();
    this.subscribeDynamicFilters();
    this.subscribeQueryParams();

    this.getCollection();
  }

  ngAfterViewInit(): void {
    this.afterViewInit();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.resizeObserver.disconnect();

    if (this.tableWrapperViewChild) {
      handyScroll.destroy(this.tableWrapperViewChild.nativeElement);
    }
  }

  afterViewInit(): void {
    const element = this.inModal ? this.elementRef.nativeElement.closest('.p-dialog-content') : this.elementRef.nativeElement;

    if (!!element) {
      this.resizeObserver.observe(element);
    }

    this.getSectionBoudingClientRect();

    if (this.disabledHandyScroll) {
      return;
    }

    this.tableWrapperViewChild = this.table?.wrapperViewChild;

    if (!!this.tableWrapperViewChild) {
      handyScroll.mount(this.tableWrapperViewChild.nativeElement);
    }
  }

  changePage(event: { page: number; size: number }): void {
    this.page = event.page;
    this.rows = event.size;

    this.saveConfiguration(
      {
        rows: this.rows,
      },
      'page'
    );

    this.getCollection();
  }

  filterCollection(data: any, moduleName: string = ''): void {
    const currentStaticFilters = this.staticFilters;

    this.staticFilters = this.preparedStaticFilters(data, moduleName);

    if (!isEqual(this.staticFilters, currentStaticFilters)) {
      this.saveConfiguration(data, 'static-filters');

      this.page = 0;

      this.getCollection('filters');
    }
  }

  loadCollection(event: any): void {
    const { sortField, sortOrder } = event;

    const sort = `${sortField},${sortOrder === 1 ? 'asc' : 'desc'}`;

    if (sortField && sort !== this.sort) {
      this.sort = sort;
      this.saveConfiguration(this.sort, 'sort');
      this.getCollection('sort');
      return;
    }

    this.filtersChange$.next({ viewName: this.viewName, data: event.filters });

    setTimeout(() => this.updateHandyScroll());
  }

  onActiveColumnsChange(init: boolean = false): void {
    this.columnsState = this.columns.reduce(
      (currentState, { field }) => ({
        ...currentState,
        [field]: this.activeColumns.includes(field),
      }),
      {}
    );

    if (!init) {
      this.saveColumnConfiguration();
    }
  }

  preparedColumns(columns: Column[], prefix?: string): Column[] {
    const data = columns.map(({ field, header, ...rest }) => {
      const fieldHeader = header || field;

      return {
        field,
        header: prefix ? `${prefix}.${fieldHeader}` : fieldHeader,
        ...rest,
      };
    });

    this.activeColumns = this.getActiveColumnByViewConvifiguration(
      columns.map(({ field }) => field),
      columns
    );
    this.columns = data;

    this.onActiveColumnsChange(true);

    return data;
  }

  subscribeQueryParams(): void {
    this.route.queryParams.subscribe(data => {
      const previewId = +data.id;

      if (!!previewId) {
        this.viewParameters.dynamicFilters.id = {
          value: previewId,
          matchMode: 'equals',
        };
      }
    });
  }

  protected getActiveColumnByViewConvifiguration(activeColumns: string[], columns?: Column[]): string[] {
    const columnsConfiguration = columns ? columns : this.columns;

    return activeColumns.filter((column: string) => {
      const columnConfig = columnsConfiguration.find(({ field, key }) => field === column || key === column);

      const columnField = columnConfig?.key || column;
      const columnConfiguration = this.viewConfiguration?.screenColumns?.find(({ columnName }) => columnName === columnField);

      return columnConfiguration ? columnConfiguration.visible : true;
    });
  }

  protected getMatchMode(matchMode: FilterMatchMode, value?: any): string {
    const valueIsDate = (value: any): boolean =>
      value && (value instanceof Date || moment.isMoment(value) || new RegExp(DATE_REGEX).test(value));

    if (Array.isArray(value) && !valueIsDate(value[0])) {
      return FilterMatchMode.IN;
    }

    switch (matchMode) {
      case 'lt':
        return 'lessThan';
      case 'lte':
        return 'lessThanOrEqual';
      case 'gt':
        return 'greaterThan';
      case 'gte':
        return 'greaterThanOrEqual';
      case 'notContains':
        return 'doesNotContain';
      default:
        return matchMode as string;
    }
  }

  private getSectionBoudingClientRect(force: boolean = false) {
    const modalElement = this.elementRef.nativeElement.closest('.p-dialog-content');
    const bodyElement = modalElement ? null : window.document.body;
    const element = modalElement ?? bodyElement;

    const sectionBoudingClientRect = element.getBoundingClientRect();

    this.sectionBoudingClientRect = {
      width: sectionBoudingClientRect.width,
      height: sectionBoudingClientRect.height,
      top: sectionBoudingClientRect.top,
      left: sectionBoudingClientRect.left,
    };
  }

  protected saveColumnConfiguration(): void {
    if (!this.screen) {
      return;
    }

    this.viewConfigurationService
      .saveConfiguration({
        screen: this.screen,
        screenColumns: Object.entries(this.columnsState).map(([field, visible]) => {
          const columnConfig = this.columns.find(({ field: columnField }) => columnField === field);

          return {
            export: columnConfig?.export === undefined ? true : !!columnConfig?.export,
            columnName: columnConfig?.key || field,
            orderNr: 0,
            visible,
          };
        }),
      })
      .subscribe();
  }

  protected subscribeDynamicFilters(): void {
    this.filtersChange$.pipe(debounceTime(300), takeUntil(this.destroy$)).subscribe((value: any) => {
      const currentDynamicFilters = this.dynamicFilters;
      const { viewName, data: filters } = value ?? {};

      if (viewName === this.viewName) {
        this.dynamicFilters = this.preparedDynamicFilters({ ...filters });
      } else {
        this.saveConfiguration(filters, 'dynamic-filters', viewName);
        return;
      }

      if (!isEqual(this.dynamicFilters, currentDynamicFilters)) {
        this.saveConfiguration(filters, 'dynamic-filters', viewName);

        this.page = 0;

        this.getCollection('filters');
      }
    });
  }

  protected loadViewConfiguration(): void {
    if (!this.viewName) {
      return;
    }

    const sort = this.sessionStorage.getItem(`${this.viewName}-sort`);
    const page = this.sessionStorage.getItem(`${this.viewName}-page`);
    const staticFilters = this.sessionStorage.getItem(`${this.viewName}-static-filters`);
    const dynamicFilters = this.sessionStorage.getItem(`${this.viewName}-dynamic-filters`);

    this.viewParameters = {
      dynamicFilters: Object.entries<{ value: any; matchMode: string }>(this.viewParameters.dynamicFilters).reduce(
        (filters: object, [key, data]: [string, { value: any; matchMode: string }]) => ({
          ...filters,
          [key]: { value: null, matchMode: data.matchMode },
        }),
        {}
      ),
      staticFilters: {},
    };

    if (!!staticFilters) {
      this.viewParameters.staticFilters = this.preparedLoadFilters(staticFilters);
      this.staticFilters = this.preparedStaticFilters(staticFilters);
    }

    if (!!dynamicFilters) {
      this.viewParameters.dynamicFilters = this.preparedLoadFilters(dynamicFilters, true);
      this.dynamicFilters = this.preparedDynamicFilters(dynamicFilters);
    }

    if (!!page) {
      this.rows = page?.rows || this.rows;
    }

    this.activeColumns = this.getActiveColumnByViewConvifiguration(this.activeColumns);

    if (!!sort) {
      const [field, order] = sort.split(',');

      this.viewParameters.sortField = field;
      this.viewParameters.sortOrder = order === 'asc' ? 1 : -1;

      this.sort = sort;
    }
  }

  private preparedDynamicFilters(data: any): any {
    const valueIsDate = (value: any): boolean =>
      value && (value instanceof Date || moment.isMoment(value) || new RegExp(DATE_REGEX).test(value));

    return Object.entries(data)
      .filter(([key, { value }]: any[]) => (Array.isArray(value) ? !!value.length : value !== null && value !== '' && value !== undefined))
      .reduce((previousValue, [key, { value, matchMode }]: any[]) => {
        const isDate = Array.isArray(value) ? valueIsDate(value[0]) : valueIsDate(value);
        const isWithTime = this.filtersWithTime.includes(key);
        const mode = this.getMatchMode(matchMode, value);
        let filterValue = value;

        if (Array.isArray(value) && isDate) {
          const isSeperateFilterDate = this.seperateFilterDates.includes(key);
          const dateToNextDay = this.filtersWithDateToNextDay.includes(key);
          const oneDateSelected = value.filter(Boolean).length < 2;
          const isEqualDay = oneDateSelected && ['lessThanOrEqual'].includes(mode);

          if (oneDateSelected && mode === 'equals') {
            return {
              ...previousValue,
              [`${key}${isSeperateFilterDate ? 'From' : ''}.greaterThanOrEqual`]: value[0]
                ? moment(value[0]).startOf('day').toDate()
                : null,
              [`${key}${isSeperateFilterDate ? 'To' : ''}.lessThanOrEqual`]: value[0]
                ? dateToNextDay
                  ? moment(value[0]).startOf('day').add(1, 'd').toDate()
                  : moment(value[0]).endOf('day')
                : null,
            };
          }

          return {
            ...previousValue,
            [`${key}${isSeperateFilterDate ? 'From' : ''}.${oneDateSelected ? mode : 'greaterThanOrEqual'}`]: value[0]
              ? isEqualDay
                ? moment(value[0]).endOf('day').toDate()
                : moment(value[0]).startOf('day').toDate()
              : null,
            [`${key}${isSeperateFilterDate ? 'To' : ''}.lessThanOrEqual`]: value[1]
              ? dateToNextDay
                ? moment(value[1]).startOf('day').add(1, 'd').toDate()
                : moment(value[1]).endOf('day').toDate()
              : null,
          };
        }

        if (!isWithTime) {
          filterValue =
            isDate && ['greaterThan', 'lessThanOrEqual'].includes(mode)
              ? moment(value).set({ h: 23, m: 59, s: 59, ms: 59 }).toDate()
              : value;
        } else {
          filterValue = isDate ? moment(value).set({ s: 0, ms: 0 }).toDate() : value;
        }

        if (Array.isArray(filterValue)) {
          filterValue = filterValue.filter((val: any) => val !== null && val !== undefined);
        }

        return {
          ...previousValue,
          [`${key}.${mode}`]: filterValue,
        };
      }, {});
  }

  private preparedStaticFilters(data: any, moduleName: string = ''): any {
    const putLessThanValue = ['auctions-day-ahead-energy-auctions', 'auctions-day-ahead-capacity-auctions'].includes(
      this.viewName as string
    );
    return Object.entries(data)
      .filter(([key, value]: any[]) => (Array.isArray(value) ? !!value.length : value !== null && value !== ''))
      .reduce((previousValue, [key, value]: any[]) => {
        if (typeof value === 'string') {
          value = new Date(value);
        }
        if (typeof value === 'object' && !Array.isArray(value)) {
          let filterValues = {};

          if (value instanceof Date) {
            const dateToNextDay = this.filtersWithDateToNextDay.includes(key);

            filterValues = {
              [`${key}.greaterThanOrEqual`]: value ? moment(value).startOf('day') : value,
              [`${key}.${moduleName === 'day-ahead' || putLessThanValue ? 'lessThan' : 'lessThanOrEqual'}`]: value
                ? dateToNextDay
                  ? moment(value).startOf('day').add(1, 'd')
                  : moment(value).endOf('day')
                : value,
            };
          } else if (isEqual(Object.keys(value), ['from', 'to'])) {
            const setRangeDate = this.setRangeFilterDateTime.includes(key);
            const isWithTime = this.filtersWithTime.includes(key);
            const fromDate =
              setRangeDate && value.from
                ? isWithTime
                  ? moment(value.from).set({ s: 0, ms: 0 })
                  : moment(value.from).startOf('day')
                : value.from;
            const toDate =
              setRangeDate && value.to ? (isWithTime ? moment(value.to).set({ s: 0, ms: 0 }) : moment(value.to).endOf('day')) : value.to;
            const isSeperateFilterDate = this.seperateFilterDates.includes(key);

            filterValues = {
              [`${key}${isSeperateFilterDate ? 'From' : ''}.greaterThanOrEqual`]: fromDate,
              [`${key}${isSeperateFilterDate ? 'To' : ''}.lessThanOrEqual`]: toDate,
            };
          } else {
            filterValues = Object.entries(value)
              .filter((valueKey, valueData) => valueData !== null)
              .reduce((valuePreviousValue, [valueKey, valueData]) => {
                const valueMatchMode = Array.isArray(valueData) ? 'in' : 'contains';

                return {
                  ...valuePreviousValue,
                  [`${key}${Helpers.capitalize(valueKey)}.${valueMatchMode}`]: valueData,
                };
              }, {});
          }

          return { ...previousValue, ...filterValues };
        }

        const matchMode = Array.isArray(value)
          ? FilterMatchMode.IN
          : !isNaN(value) || value instanceof Date
          ? FilterMatchMode.EQUALS
          : FilterMatchMode.CONTAINS;

        return {
          ...previousValue,
          [`${key}.${this.getMatchMode(matchMode)}`]: value,
        };
      }, {});
  }

  protected updateHandyScroll(): void {
    if (this.tableWrapperViewChild) {
      handyScroll.update(this.tableWrapperViewChild.nativeElement);
    }
  }

  private preparedLoadFilters(data: any, isDynamic: boolean = false): any {
    return Object.entries(data).reduce((currentData, [key, keyValue]: [string, any]) => {
      const dateKeys = [...this.seperateFilterDates, ...this.setRangeFilterDateTime, ...this.filtersWithTime];
      const value = isDynamic && keyValue ? keyValue.value : keyValue;
      let isDate = new RegExp(DATE_REGEX).test(value);

      if (!isDynamic && value && typeof value === 'object' && isEqual(Object.keys(value), ['from', 'to'])) {
        return {
          ...currentData,
          [key]: {
            from: value.from ? moment(value.from).toDate() : null,
            to: value.to ? moment(value.to).toDate() : null,
          },
        };
      }

      let filterValue = value;

      if (Array.isArray(value)) {
        isDate = value.every((val: string) => RegExp(DATE_REGEX).test(val)) || dateKeys.includes(key);

        if (isDate) {
          const startDate = value[0] ? moment(value[0]).toDate() : value[0];
          const endDate = value[1] ? moment(value[1]).toDate() : value[1];

          filterValue = [startDate, endDate];
        }
      } else {
        filterValue = isDate ? moment(value).toDate() : value;
      }

      if (Array.isArray(filterValue)) {
        filterValue = filterValue.filter((value: any) => value !== null && value !== 'null');
      }

      return {
        ...currentData,
        [key]: isDynamic ? { ...keyValue, value: filterValue } : filterValue,
      };
    }, {});
  }

  private saveConfiguration(data: any, type: string, viewName?: string): void {
    const viewStoreKey = viewName ?? this.viewName;
    if (!viewStoreKey) {
      return;
    }

    Object.entries(data).forEach((item: any[]) => {
      const value = item[1]?.value;
      if (Array.isArray(value) && value.every(v => isNil(v))) {
        item[1].value = null;
      }
    });

    const key = `${viewStoreKey}-${type}`;
    this.sessionStorage.setItem(key, data);
  }
}
