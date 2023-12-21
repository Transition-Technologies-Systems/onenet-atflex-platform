import { SchedulingUnitsFiltersComponent, SchedulingUnitsFiltersService } from './filters';
import { SchedulingUnitsListComponent, SchedulingUnitsListStore } from './tabs/list';
import { SchedulingUnitsTypesComponent, SchedulingUnitsTypesService, SchedulingUnitsTypesStore } from './tabs/types-su';

import { AccordionModule } from 'primeng/accordion';
import { NgModule } from '@angular/core';
import { ProductsSharedModule } from '../products/shared';
import { SchedulingUnitsComponent } from './scheduling-units.component';
import { SchedulingUnitsConfirmDialogComponent } from './dialog/confirm';
import { SchedulingUnitsDialogComponent } from './dialog';
import { SchedulingUnitsPreviewComponent } from './preview/preview.component';
import { SchedulingUnitsRoutingModule } from './scheduling-units.routing';
import { SchedulingUnitsService } from './scheduling-units.service';
import { SelectButtonModule } from 'primeng/selectbutton';
import { SharedModule } from '@app/shared';

@NgModule({
  imports: [SelectButtonModule, SharedModule, SchedulingUnitsRoutingModule, AccordionModule, ProductsSharedModule],
  declarations: [
    SchedulingUnitsComponent,
    SchedulingUnitsDialogComponent,
    SchedulingUnitsFiltersComponent,
    SchedulingUnitsPreviewComponent,
    SchedulingUnitsConfirmDialogComponent,

    SchedulingUnitsListComponent,

    SchedulingUnitsTypesComponent,
  ],
  providers: [
    SchedulingUnitsFiltersService,
    SchedulingUnitsService,
    SchedulingUnitsTypesService,

    SchedulingUnitsListStore,
    SchedulingUnitsTypesStore,
  ],
})
export class SchedulingUnitsModule {}
