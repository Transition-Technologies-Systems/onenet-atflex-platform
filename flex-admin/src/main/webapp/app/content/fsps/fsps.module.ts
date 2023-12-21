import { AccordionModule } from 'primeng/accordion';

import { NgModule } from '@angular/core';
import { SharedModule } from '@app/shared';

import { FspsDialogComponent, FspsDialogService } from './dialog';
import { FspsFiltersComponent, FspsFiltersService } from './filters';
import { FspsComponent } from './fsps.component';
import { FspsRoutingModule } from './fsps.routing';
import { FspsService } from './fsps.service';
import { FspsStore } from './fsps.store';

@NgModule({
  imports: [SharedModule, FspsRoutingModule, AccordionModule],
  declarations: [FspsComponent, FspsDialogComponent, FspsFiltersComponent],
  providers: [FspsService, FspsStore, FspsDialogService, FspsFiltersService],
})
export class FspsModule {}
