import { NgModule } from '@angular/core';
import { SharedLibraryModule } from '@app/shared/shared-library.module';

import { SharedCommonsModule } from '../shared-commons.module';
import { FilterContainerDirective } from './filter-container/filter-container.directive';
import { FilterGroupComponent } from './filter-group/filter-group.component';
import { FilterTabDirective } from './filter-tab/filter-tab.directive';
import { FilterTemplateDirective } from './filter/filter-template.directive';
import { FilterDirective } from './filter/filter.directive';
import { FiltersComponent } from './filters.component';

@NgModule({
  imports: [SharedCommonsModule, SharedLibraryModule],
  declarations: [
    FiltersComponent,
    FilterContainerDirective,
    FilterDirective,
    FilterTabDirective,
    FilterTemplateDirective,
    FilterGroupComponent,
  ],
  exports: [FiltersComponent, FilterContainerDirective, FilterDirective, FilterTabDirective, FilterTemplateDirective, FilterGroupComponent],
})
export class FiltersModule {}
