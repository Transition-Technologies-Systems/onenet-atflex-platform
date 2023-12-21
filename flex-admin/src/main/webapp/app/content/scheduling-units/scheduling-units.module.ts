import { SchedulingUnitsFiltersComponent, SchedulingUnitsFiltersService } from './filters';

import { AccordionModule } from 'primeng/accordion';
import { NgModule } from '@angular/core';
import { ProductsSharedModule } from '../products/shared';
import { SchedulingUnitsComponent } from './scheduling-units.component';
import { SchedulingUnitsConfirmDialogComponent } from './dialog/confirm';
import { SchedulingUnitsDialogComponent } from './dialog';
import { SchedulingUnitsPreviewComponent } from './preview/preview.component';
import { SchedulingUnitsRoutingModule } from './scheduling-units.routing';
import { SchedulingUnitsService } from './scheduling-units.service';
import { SchedulingUnitsStore } from './scheduling-units.store';
import { SharedModule } from '@app/shared';

@NgModule({
  imports: [SharedModule, SchedulingUnitsRoutingModule, AccordionModule, ProductsSharedModule],
  declarations: [
    SchedulingUnitsComponent,
    SchedulingUnitsDialogComponent,
    SchedulingUnitsFiltersComponent,
    SchedulingUnitsPreviewComponent,
    SchedulingUnitsConfirmDialogComponent,
  ],
  providers: [SchedulingUnitsFiltersService, SchedulingUnitsService, SchedulingUnitsStore],
})
export class SchedulingUnitsModule {}
