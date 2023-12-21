import { PartnershipFiltersComponent, PartnershipFiltersService } from './filters';

import { NgModule } from '@angular/core';
import { PartnershipComponent } from './partnership.component';
import { PartnershipRoutingModule } from './partnership.routing';
import { PartnershipService } from './partnership.service';
import { SelectButtonModule } from 'primeng/selectbutton';
import { SharedModule } from '@app/shared';

@NgModule({
  imports: [SharedModule, PartnershipRoutingModule, SelectButtonModule],
  declarations: [PartnershipComponent, PartnershipFiltersComponent],
  providers: [PartnershipService, PartnershipFiltersService],
})
export class PartnershipModule {}
