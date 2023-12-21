import { AccordionModule } from 'primeng/accordion';
import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { SchedulingUnitPreviewComponent } from './preview/preview.component';
import { SharedCommonsModule } from '@app/shared/commons/shared-commons.module';
import { TranslateModule } from '@ngx-translate/core';

@NgModule({
  declarations: [SchedulingUnitPreviewComponent],
  imports: [CommonModule, AccordionModule, TranslateModule, SharedCommonsModule],
  exports: [SchedulingUnitPreviewComponent, TranslateModule],
})
export class SchedulingUnitsSharedModule {}
