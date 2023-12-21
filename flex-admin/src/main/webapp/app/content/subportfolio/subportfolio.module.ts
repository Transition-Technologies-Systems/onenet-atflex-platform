import { AccordionModule } from 'primeng/accordion';

import { NgModule } from '@angular/core';
import { SharedModule } from '@app/shared';

import { SubportfoliosDialogComponent } from './dialog';
import { SubportfoliosFiltersComponent, SubportfoliosFiltersService } from './filters';
import { SubportfoliosPreviewComponent } from './preview/preview.component';
import { SubportfoliosComponent } from './subportfolio.component';
import { SubportfoliosRoutingModule } from './subportfolio.routing';
import { SubportfoliosService } from './subportfolio.service';
import { SubportfoliosStore } from './subportfolio.store';

@NgModule({
  imports: [SharedModule, SubportfoliosRoutingModule, AccordionModule],
  declarations: [SubportfoliosComponent, SubportfoliosDialogComponent, SubportfoliosFiltersComponent, SubportfoliosPreviewComponent],
  providers: [SubportfoliosFiltersService, SubportfoliosService, SubportfoliosStore],
})
export class SubportfoliosModule {}
