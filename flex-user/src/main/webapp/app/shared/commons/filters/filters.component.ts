import {
  AfterViewInit,
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  ContentChildren,
  ElementRef,
  EventEmitter,
  Input,
  NgZone,
  Output,
  QueryList,
  TemplateRef,
  ViewChild,
} from '@angular/core';
import { BoundingBox, Dictionary } from '@app/shared/models';
import { UntypedFormControl, UntypedFormGroup } from '@angular/forms';
import { Subscription, merge } from 'rxjs';

import { FilterContainerContext } from './filter-container-context';
import { FilterContainerDirective } from './filter-container/filter-container.directive';
import { FilterContext } from './filter-context';
import { FilterTabContext } from './filter-tab-context';
import { FiltersService } from './filters.service';
import { skip } from 'rxjs/operators';

@Component({
  selector: 'app-filters',
  styleUrls: ['./filters.component.scss'],
  templateUrl: './filters.component.html',
  providers: [FiltersService],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FiltersComponent implements AfterViewInit {
  @ViewChild('inputFilter', { static: true }) inputEl: TemplateRef<any> | undefined;
  @ViewChild('inputNumberFilter', { static: true }) inputNumberEl: TemplateRef<any> | undefined;
  @ViewChild('selectFilter', { static: true }) selectEl: TemplateRef<any> | undefined;
  @ViewChild('checkboxFilter', { static: true }) checkboxEl: TemplateRef<any> | undefined;
  @ViewChild('multiSelectFilter', { static: true }) multiSelectEl: TemplateRef<any> | undefined;
  @ViewChild('dateFilter', { static: true }) dateEl: TemplateRef<any> | undefined;
  @ViewChild('dateWithTimeFilter', { static: true }) dateTimeEl: TemplateRef<any> | undefined;
  @ViewChild('dateRangeFilter', { static: true }) dateRangeEl: TemplateRef<any> | undefined;
  @ViewChild('radioButtonFilter', { static: true }) radioButtonFilterEl: TemplateRef<any> | undefined;
  @ViewChild('checkboxMultipleFilter', { static: true }) checkboxMultipleFilterEl: TemplateRef<any> | undefined;

  @Input() hideTranslation = 'filters.hide';
  @Input() showTranslation = 'filters.show';
  @Input() additionalToogleName: string | undefined;
  @Input() hideToogle = false;
  @Input() state = 'default';

  @Input() form = new UntypedFormGroup({});
  @Input() showFilters = true;

  @Output() getData = new EventEmitter();
  @Output() toogleChange = new EventEmitter<boolean>();

  /**
   * Set column templates
   */
  @ContentChildren(FilterContainerDirective)
  set filterContainers(filterContainers: QueryList<FilterContainerDirective>) {
    this.filterContainersDirective = filterContainers;

    if (filterContainers) {
      this.updateContainersContext();

      if (this.initialized) {
        this.setFiltersToPrint();
      }
    }
  }

  get filterContainers(): QueryList<FilterContainerDirective> {
    return this.filterContainersDirective;
  }

  get containerTabs(): FilterTabContext[] {
    if (!this.activeContainer) {
      return [];
    }

    if (!this.activeContainer.tabs) {
      return [];
    }

    return this.activeContainer.tabs.filter(({ visible }) => visible);
  }

  get containerClass(): string {
    const width = this.elementBoundingClientRect.width;

    return width < 1024 ? 'filters-container row-style' : 'filters-container';
  }

  activeTab = 0;
  configurationLoaded = false;
  filtersContext: FilterContainerContext[] = [];
  activeContainer: FilterContainerContext | undefined;

  private initialized = false;
  private isChangeDetectionPending = false;
  private subscription = new Subscription();
  private runAfterChangeDetection: Array<() => void> = [];
  private elementBoundingClientRect: BoundingBox = { top: 0, left: 0, height: 0, width: 0 };
  private filterContainersDirective: QueryList<FilterContainerDirective> = new QueryList<FilterContainerDirective>();

  private resizeObserver = new ResizeObserver(() => {
    this.getElementBoudingClientRect();
  });

  constructor(private ngZone: NgZone, private elementRef: ElementRef, private cdr: ChangeDetectorRef, private service: FiltersService) {}

  ngAfterViewInit(): void {
    this.updateContainersContext();
    this.preparedFilters();

    this.subscribeGroupChange();
    this.subscribeTemplateChange();

    this.getElementBoudingClientRect();

    this.resizeObserver.observe(this.elementRef.nativeElement.querySelector('form'));
  }

  changeTab(index: number): void {
    this.activeTab = index;
  }

  clearFilter(): void {
    this.form.reset();
    this.service.filterChange();
  }

  filter(): void {
    this.getData.emit(null);
  }

  getDictionaryName(items: any[], dictionaries: Dictionary[]): Dictionary[] {
    if (!items || !items.length) {
      return [];
    }

    return dictionaries.filter(({ value }) => items.includes(value));
  }

  getFiltersToPrint(context: FilterContainerContext): FilterContext[] {
    let filters = [];

    if (!context.tabs) {
      filters = context.filters || [];
    } else {
      const tab = context.tabs[this.activeTab || 0];

      filters = tab ? tab.filters : [];
    }

    return filters.filter((filter: FilterContext) => filter.visible);
  }

  getFormControl(filter: FilterContext): UntypedFormControl | null {
    if (!filter.controlName || !this.form) {
      return null;
    }

    return this.form.get(filter.controlName) as UntypedFormControl;
  }

  getTemplate(filter: FilterContext): TemplateRef<any> | null {
    let template: TemplateRef<any> | undefined;

    if (filter.template) {
      return filter.template;
    }

    switch (filter.type) {
      case 'input':
        template = this.inputEl;
        break;
      case 'input-number':
        template = this.inputNumberEl;
        break;
      case 'select':
        template = this.selectEl;
        break;
      case 'multi-select':
        template = this.multiSelectEl;
        break;
      case 'date':
        template = this.dateEl;
        break;
      case 'date-time':
        template = this.dateTimeEl;
        break;
      case 'date-range':
        template = this.dateRangeEl;
        break;
      case 'checkbox':
        template = this.checkboxEl;
        break;
      case 'checkbox-multiple':
        template = this.checkboxMultipleFilterEl;
        break;
      case 'radiobutton':
        template = this.radioButtonFilterEl;
        break;
      default:
        template = this.inputEl;
    }

    return template ? template : null;
  }

  toggle(): void {
    this.showFilters = !this.showFilters;

    this.toogleChange.next(this.showFilters);
  }

  /**
   * Active first group
   */
  private activateFirstGroup(): void {
    const [container] = this.filtersContext;

    if (!container || this.initialized) {
      return;
    }

    setTimeout(() => {
      this.activeTab = 0;
      this.initialized = true;
      this.activeContainer = container;
      this.service.activeGroup = container.group;

      this.preparedVisibilityConfiguration(container);

      this.cdr.markForCheck();
    });
  }

  /**
   * Run change detection
   */
  private doChangeDetection(): void {
    const runAfterChangeDetection = this.runAfterChangeDetection;

    this.isChangeDetectionPending = false;
    this.runAfterChangeDetection = [];

    this.ngZone.run(() => this.cdr.markForCheck());

    for (const fn of runAfterChangeDetection) {
      fn();
    }
  }

  private getElementBoudingClientRect(): void {
    const elementBoundingClientRect = this.elementRef.nativeElement.querySelector('form').getBoundingClientRect();

    this.elementBoundingClientRect = {
      width: elementBoundingClientRect.width,
      height: elementBoundingClientRect.height,
      top: elementBoundingClientRect.top,
      left: elementBoundingClientRect.left,
    };
  }

  private mapVisibleFilter(filters: FilterContext[] | null, context: FilterContainerContext): FilterContext[] | null {
    if (!filters) {
      return null;
    }

    return filters.map((filter: FilterContext) => ({
      ...filter,
      visible: true,
    }));
  }

  /**
   * Mark change detection needed - run after resolve, once time
   *
   * @param runAfter Run function after detect changes
   */
  private markChangeDetectionNeeded(runAfter?: () => void): void {
    if (runAfter) {
      this.runAfterChangeDetection.push(runAfter);
    }

    if (!this.isChangeDetectionPending) {
      this.isChangeDetectionPending = true;
      this.ngZone.runOutsideAngular(() => Promise.resolve().then(() => this.doChangeDetection()));
    }
  }

  /**
   * Prepared filters
   */
  private preparedFilters(): void {
    if (!this.filtersContext.length) {
      return;
    }

    this.markChangeDetectionNeeded();
  }

  /**
   * Prepared visibility configuration
   *
   * @param container The active filter container
   */
  private preparedVisibilityConfiguration(container: FilterContainerContext): void {
    this.showFilters = true;

    this.setVisibilityFiltersAndTabs();
  }

  /**
   * Set filters to print and emit new configuation
   *
   * @param resetActiveTab Reset active tab
   */
  private setFiltersToPrint(resetActiveTab: boolean = false): void {
    const container = this.filtersContext.find(({ group }) => group === this.service.activeGroup);

    if (!container) {
      return;
    }

    if (resetActiveTab) {
      this.activeTab = 0;
    }

    this.activeContainer = container;
    this.preparedVisibilityConfiguration(container);
  }

  /**
   * Set visibility for filters and tabs
   */
  private setVisibilityFiltersAndTabs(container?: FilterContainerContext): void {
    this.filtersContext = this.filtersContext.map((context: FilterContainerContext) => {
      if (!container || context.group === container.group) {
        return {
          ...context,
          tabs: !context.tabs
            ? null
            : context.tabs.map((value: FilterTabContext, tabIndex: number) => {
                if (container && this.activeTab !== tabIndex) {
                  return value;
                }

                const filters = this.mapVisibleFilter(value.filters, context) || [];
                const visible = filters.some(filter => filter.visible);

                return {
                  ...value,
                  visible,
                  filters,
                };
              }),
          filters: this.mapVisibleFilter(context?.filters, context),
        };
      }

      return context;
    });

    this.activeContainer = this.filtersContext.find(({ group }) => group === this.service.activeGroup);
  }

  /**
   * Subscribe group change
   */
  private subscribeGroupChange(): void {
    this.subscription.add(
      this.service.activeGroup$.pipe(skip(1)).subscribe(() => {
        this.setFiltersToPrint(true);
        this.cdr.markForCheck();
      })
    );
  }

  /**
   * Subscribe filters template change
   */
  private subscribeTemplateChange(): void {
    this.subscription.add(
      this.filterContainers.changes.subscribe((filterContainers: QueryList<FilterContainerDirective>) => {
        this.filterContainersDirective = filterContainers;
        this.preparedFilters();
      })
    );

    this.subscription.add(
      merge(this.service.groupChange$, this.service.filterChange$).subscribe(() => {
        this.filterContainers.notifyOnChanges();

        this.updateContainersContext();
      })
    );
  }

  /**
   * Update filters context
   */
  private updateContainersContext(): void {
    this.filtersContext = this.service.getFilterContainerContext(this.filterContainersDirective.toArray());

    if (this.initialized) {
      const container = this.filtersContext.find(({ group }) => group === this.service.activeGroup);

      if (container) {
        this.activeContainer = container;

        this.preparedVisibilityConfiguration(container);
      }

      this.cdr.markForCheck();

      return;
    }

    this.activateFirstGroup();
  }
}
