import { Subscription } from 'rxjs';

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
import { FilterDirective } from '../filter/filter.directive';
import { FiltersService } from '../filters.service';

/**
 * Directive for declare filters group container
 *
 * @example
 * <app-filter-tab>
 *  <app-filter></app-filter>
 * </app-filter-tab>
 */
@Directive({
  // tslint:disable-next-line:directive-selector
  selector: 'app-filter-tab',
})
export class FilterTabDirective implements OnChanges, OnDestroy, AfterViewInit {
  /**
   * Tab name
   */
  @Input() tabName = '';

  /**
   * Set filters templates
   */
  @ContentChildren(FilterDirective)
  set filterTemplates(filters: QueryList<FilterDirective>) {
    this.filterDirectives = filters;

    if (filters) {
      this.filtersContext = this.filtersService.getFilterTemplatesContext(filters.toArray());
    }
  }

  get filterTemplates(): QueryList<FilterDirective> {
    return this.filterDirectives;
  }

  filtersContext: FilterContext[] = [];

  private subscription = new Subscription();
  private filterDirectives = new QueryList<FilterDirective>();

  constructor(private filtersService: FiltersService) {}

  ngAfterViewInit(): void {
    this.subscription.add(
      this.filterTemplates.changes.subscribe((filters: QueryList<FilterDirective>) => {
        this.filterDirectives = filters;
        this.filtersService.tabChange();
      })
    );

    this.subscription.add(
      this.filtersService.filterChange$.subscribe(() => {
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
}
