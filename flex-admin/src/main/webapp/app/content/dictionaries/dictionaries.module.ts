import { NgModule } from '@angular/core';
import { SharedModule } from '@app/shared';
import { AccordionModule } from 'primeng/accordion';
import { DictionariesComponent } from './dictionaries.component';
import { DictionariesRoutingModule } from './dictionaries.routing';
import { DictionariesService } from './dictionaries.service';
import { DictionariesStore } from './dictionaries.store';
import { DictPreviewComponent } from './preview/preview.component';
import { DictDialogComponent } from './dialog/dialog.component';
import { KdmModelsDialogComponent } from './kdm-models-dialog/kdm-models-dialog.component';

@NgModule({
  imports: [SharedModule, DictionariesRoutingModule, AccordionModule],
  declarations: [DictionariesComponent, DictPreviewComponent, DictDialogComponent, KdmModelsDialogComponent],
  providers: [DictionariesService, DictionariesStore],
})
export class DictionariesModule {}
