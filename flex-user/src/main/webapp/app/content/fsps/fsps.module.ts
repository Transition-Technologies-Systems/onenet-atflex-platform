import { FspsFiltersComponent, FspsFiltersService } from './filters';

import { AccordionModule } from 'primeng/accordion';
import { FspsComponent } from './fsps.component';
import { FspsRoutingModule } from './fsps.routing';
import { FspsService } from './fsps.service';
import { FspsStore } from './fsps.store';
import { NgModule } from '@angular/core';
import { SharedModule } from '@app/shared';

@NgModule({
  imports: [SharedModule, FspsRoutingModule, AccordionModule],
  declarations: [FspsComponent, FspsFiltersComponent],
  providers: [FspsService, FspsStore, FspsFiltersService],
})
export class FspsModule {}
