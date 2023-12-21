import { NgModule } from '@angular/core';
import { KpiComponent } from './kpi.component';
import { SharedModule } from '@app/shared';
import { KpiRoutingModule } from './kpi.routing';
import { AccordionModule } from 'primeng/accordion';
import { KpiService } from './kpi.service';
import { KpiStore } from './kpi.store';
import { KpiFiltersComponent } from './filters/filters.component';
import { KpiFiltersService } from './filters/filters.service';

@NgModule({
  declarations: [KpiComponent, KpiFiltersComponent],
  imports: [SharedModule, KpiRoutingModule, AccordionModule],
  providers: [KpiService, KpiStore, KpiFiltersService],
})
export class KpiModule {}
