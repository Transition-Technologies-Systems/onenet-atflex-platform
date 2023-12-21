import { Component, Input } from '@angular/core';

import { FiltersService } from '../filters.service';

/**
 * Component to declare filter group
 *
 * @example
 * <app-filter-group>
 *  name
 * </app-filter-group>
 */
@Component({
  selector: 'app-filter-group',
  templateUrl: './filter-group.component.html',
  styleUrls: ['./filter-group.component.scss'],
})
export class FilterGroupComponent {
  /**
   * Key to identify group
   */
  @Input() key = 'group';

  activeGroup$ = this.filtersService.activeGroup$;

  constructor(private filtersService: FiltersService) {}

  changeGroup(): void {
    this.filtersService.activeGroup = this.key;
  }
}
