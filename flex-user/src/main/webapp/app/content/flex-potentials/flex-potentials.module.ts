import { AccordionModule } from 'primeng/accordion';

import { NgModule } from '@angular/core';
import { SharedModule } from '@app/shared';

import { ProductsSharedModule } from '../products/shared';
import { FlexPotentialsDialogComponent, FlexPotentialsDialogService } from './dialog';
import { FlexPotentialsFiltersComponent, FlexPotentialsFiltersService } from './filters';
import { FlexPotentialsComponent } from './flex-potentials.component';
import { FlexPotentialsRoutingModule } from './flex-potentials.routing';
import { FlexPotentialsService } from './flex-potentials.service';
import { FlexPotentialsStore } from './flex-potentials.store';
import { FlexPotentialsPreviewComponent } from './preview';

@NgModule({
  imports: [SharedModule, FlexPotentialsRoutingModule, AccordionModule, ProductsSharedModule],
  declarations: [FlexPotentialsComponent, FlexPotentialsDialogComponent, FlexPotentialsFiltersComponent, FlexPotentialsPreviewComponent],
  providers: [FlexPotentialsService, FlexPotentialsStore, FlexPotentialsDialogService, FlexPotentialsFiltersService],
})
export class FlexPotentialsModule {}
