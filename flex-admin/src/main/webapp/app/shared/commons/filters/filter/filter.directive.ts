import { ContentChild, Directive, Input, OnChanges, SimpleChange, SimpleChanges, TemplateRef } from '@angular/core';
import { Dictionary } from '@app/shared/models';

import { FilterType } from '../filter-type';
import { FiltersService } from '../filters.service';
import { FilterTemplateDirective } from './filter-template.directive';

/**
 * Directive for declare filter
 *
 * @example
 * <app-filter>
 *  <ng-template appFilterTemplate></ng-template>
 * </app-filter>
 */
@Directive({
  // tslint:disable-next-line:directive-selector
  selector: 'app-filter',
})
export class FilterDirective implements OnChanges {
  @Input() type: FilterType = 'input';
  @Input() name = 'filter';
  @Input() showHeader = true;
  @Input() controlName: string | undefined;
  @Input() emptyOption = false;
  @Input() className: string | undefined;
  @Input() iconClass: string | undefined;
  @Input() dictionaries: Dictionary[] | null = [];
  @Input() translateDictionaries = false;
  @Input() optionLabel: string = 'label';
  @Input() optionValue: string = 'value';
  @Input() filter: boolean = false;

  /**
   * Template for filter
   */
  @ContentChild(FilterTemplateDirective, { read: TemplateRef, static: true })
  filterTemplate: TemplateRef<any> | undefined;

  constructor(private filtersService: FiltersService) {}

  /**
   * Notify about changes in the filter
   *
   * @param changes The inputs changes
   */
  ngOnChanges(changes: SimpleChanges): void {
    const isFirstChange = Object.entries(changes).every(([, change]: [string, SimpleChange]) => change.firstChange);

    if (isFirstChange) {
      return;
    }

    this.filtersService.filterChange();
  }
}
