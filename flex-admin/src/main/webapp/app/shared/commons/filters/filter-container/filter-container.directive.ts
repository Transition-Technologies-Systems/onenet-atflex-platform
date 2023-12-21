import { merge, Subscription } from 'rxjs';

import {
  AfterViewInit,
  ContentChildren,
  Directive,
  Input,
  OnChanges,
  OnDestroy,
  QueryList,
  SimpleChange,
  SimpleChanges,
} from '@angular/core';

import { FilterContext } from '../filter-context';
import { FilterTabContext } from '../filter-tab-context';
import { FilterTabDirective } from '../filter-tab/filter-tab.directive';
import { FilterDirective } from '../filter/filter.directive';
import { FiltersService } from '../filters.service';

/**
 * Directive for declare filters group container
 *
 * @example
 * <app-filter-container>
 *  <app-filter-tab></app-filter-tab>
 * </app-filter-container>
 */
@Directive({
  // tslint:disable-next-line:directive-selector
  selector: 'app-filter-container',
})
export class FilterContainerDirective implements OnChanges, OnDestroy, AfterViewInit {
  /**
   * Group key
   */
  @Input() group = 'default';

  /**
   * Set filters tab templates
   */
  @ContentChildren(FilterTabDirective)
  set filterTabTemplates(filterTabs: QueryList<FilterTabDirective>) {
    this.filterTabDirectives = filterTabs;

    if (filterTabs) {
      this.updateTabsContext();
    }
  }

  get filterTabTemplates(): QueryList<FilterTabDirective> {
    return this.filterTabDirectives;
  }

  /**
   * Set filters templates
   */
  @ContentChildren(FilterDirective)
  set filterTemplates(filters: QueryList<FilterDirective>) {
    this.filterDirectives = filters;

    if (filters) {
      this.updateContext();
    }
  }

  get filterTemplates(): QueryList<FilterDirective> {
    return this.filterDirectives;
  }

  filtersContext: FilterContext[] = [];
  filterTabsContext: FilterTabContext[] = [];

  private subscription = new Subscription();
  private filterDirectives = new QueryList<FilterDirective>();
  private filterTabDirectives = new QueryList<FilterTabDirective>();

  constructor(private filtersService: FiltersService) {}

  ngAfterViewInit(): void {
    this.updateTabsContext();
    this.updateContext();

    this.subscription.add(
      merge(this.filterTabTemplates.changes, this.filterTemplates.changes).subscribe(() => {
        this.filtersService.groupChange();

        this.updateTabsContext();
        this.updateContext();
      })
    );

    this.subscription.add(
      this.filtersService.filterChange$.subscribe(() => {
        this.filterTabTemplates.notifyOnChanges();
        this.filterTemplates.notifyOnChanges();
      })
    );
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  /**
   * Notify about changes in the filters container
   *
   * @param changes The inputs changes
   */
  ngOnChanges(changes: SimpleChanges): void {
    const isFirstChange = Object.entries(changes).every(([, change]: [string, SimpleChange]) => change.firstChange);

    if (isFirstChange) {
      return;
    }

    this.filtersService.groupChange();
  }

  private updateContext(): void {
    if (!this.filterDirectives) {
      return;
    }

    this.filtersContext = this.filtersService.getFilterTemplatesContext(this.filterDirectives.toArray());
  }

  private updateTabsContext(): void {
    if (!this.filterTabDirectives) {
      return;
    }

    this.filterTabsContext = this.filtersService.getFilterTabContext(this.filterTabDirectives.toArray());
  }
}
