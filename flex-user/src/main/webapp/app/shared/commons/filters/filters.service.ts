import { BehaviorSubject, Observable, Subject } from 'rxjs';

import { Injectable } from '@angular/core';

import { FilterContainerContext } from './filter-container-context';
import { FilterContainerDirective } from './filter-container/filter-container.directive';
import { FilterContext } from './filter-context';
import { FilterTabContext } from './filter-tab-context';
import { FilterTabDirective } from './filter-tab/filter-tab.directive';
import { FilterDirective } from './filter/filter.directive';

@Injectable()
export class FiltersService {
  get activeGroup$(): Observable<string> {
    return this.activeGroupSubject.asObservable();
  }

  get activeGroup(): string {
    return this.activeGroupSubject.getValue();
  }

  set activeGroup(value: string) {
    this.activeGroupSubject.next(value);
  }

  get filterChange$(): Observable<void> {
    return this.filterChangeSubject.asObservable();
  }

  get groupChange$(): Observable<void> {
    return this.groupChangeSubject.asObservable();
  }

  get tabChange$(): Observable<void> {
    return this.tabChangeSubject.asObservable();
  }

  private tabChangeSubject = new Subject<void>();
  private groupChangeSubject = new Subject<void>();
  private filterChangeSubject = new Subject<void>();
  private activeGroupSubject = new BehaviorSubject<string>('default');

  /**
   * Get context of filter groups
   *
   * @param directives The filters directive
   */
  getFilterContainerContext(directives: FilterContainerDirective[]): FilterContainerContext[] {
    return directives.map((directive: FilterContainerDirective) => ({
      group: directive.group,
      tabs: directive.filtersContext && directive.filtersContext.length ? null : directive.filterTabsContext,
      filters: directive.filterTabsContext && directive.filterTabsContext.length ? null : directive.filtersContext,
    }));
  }

  /**
   * Get context of filter tabs
   *
   * @param directives The filters directive
   */
  getFilterTabContext(directives: FilterTabDirective[]): FilterTabContext[] {
    return directives.map((directive: FilterTabDirective) => ({
      tabName: directive.tabName,
      filters: directive.filtersContext || [],
    }));
  }

  /**
   * Get context of filter templates
   *
   * @param directives The filters directive
   */
  getFilterTemplatesContext(directives: FilterDirective[]): FilterContext[] {
    return directives.map((directive: FilterDirective) => ({
      name: directive.name,
      type: directive.type,
      className: directive.className,
      iconClass: directive.iconClass,
      showHeader: directive.showHeader,
      controlName: directive.controlName,
      emptyOption: directive.emptyOption,
      dictionaries: directive.dictionaries || [],
      template: directive.filterTemplate || null,
      translateDictionaries: directive.translateDictionaries,
      optionLabel: directive.optionLabel,
      optionValue: directive.optionValue,
    }));
  }

  /**
   * Emit information about group change
   */
  groupChange(): void {
    this.groupChangeSubject.next();
  }

  /**
   * Emit information about filter change
   */
  filterChange(): void {
    this.filterChangeSubject.next();
  }

  /**
   * Emit information about tab change
   */
  tabChange(): void {
    this.tabChangeSubject.next();
  }
}
