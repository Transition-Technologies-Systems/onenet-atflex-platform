import { FiltersModule } from './commons/filters/filters.module';
import { NgModule } from '@angular/core';
import { NotificationsModule } from './notifications';
import { ProposalModule } from './proposal';
import { SharedCommonsModule } from './commons/shared-commons.module';
import { SharedLibraryModule } from './shared-library.module';
import { ValidatorComponent } from './validator/validator.component';
import { ValidatorContainerDirective } from './validator/validator-container.directive';

/**
 * Shared module to exports form, material
 */
@NgModule({
  declarations: [ValidatorComponent, ValidatorContainerDirective],
  imports: [FiltersModule, NotificationsModule, ProposalModule, SharedLibraryModule, SharedCommonsModule],
  exports: [FiltersModule, SharedLibraryModule, SharedCommonsModule, NotificationsModule, ProposalModule, ValidatorContainerDirective],
})
export class SharedModule {}
