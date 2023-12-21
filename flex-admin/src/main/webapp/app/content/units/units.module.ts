import { UnitsFiltersComponent, UnitsFiltersService } from './filters';
import { UnitsSelfSchedulesComponent, UnitsSelfSchedulesService } from './tabs/self-schedules';

import { AccordionModule } from 'primeng/accordion';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { GoogleMapsModule } from '@angular/google-maps';
import { NgModule } from '@angular/core';
import { SelectButtonModule } from 'primeng/selectbutton';
import { SharedModule } from '@app/shared';
import { UnitsComponent } from './units.component';
import { UnitsDialogComponent } from './dialog';
import { UnitsInviteDerComponent } from './invite-der';
import { UnitsListComponent } from './tabs/list';
import { UnitsPreviewComponent } from './preview/preview.component';
import { UnitsRoutingModule } from './units.routing';
import { UnitsSelfSchedulePreviewComponent } from './tabs/self-schedules/preview';
import { UnitsService } from './units.service';

@NgModule({
  imports: [SharedModule, UnitsRoutingModule, GoogleMapsModule, AccordionModule, SelectButtonModule, ConfirmDialogModule],
  declarations: [
    UnitsComponent,
    UnitsDialogComponent,
    UnitsFiltersComponent,
    UnitsPreviewComponent,
    UnitsInviteDerComponent,

    UnitsListComponent,
    UnitsSelfSchedulesComponent,
    UnitsSelfSchedulePreviewComponent,
  ],
  providers: [UnitsFiltersService, UnitsService, UnitsSelfSchedulesService],
})
export class UnitsModule {}
