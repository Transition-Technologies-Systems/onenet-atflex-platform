import { AccordionModule } from 'primeng/accordion';
import { CommonModule } from '@angular/common';
import { FlexPotentialPreviewComponent } from './preview/preview.component';
import { NgModule } from '@angular/core';
import { SharedCommonsModule } from '@app/shared/commons/shared-commons.module';
import { TranslateModule } from '@ngx-translate/core';

@NgModule({
  declarations: [FlexPotentialPreviewComponent],
  imports: [CommonModule, AccordionModule, TranslateModule, SharedCommonsModule],
  exports: [FlexPotentialPreviewComponent, TranslateModule],
})
export class FlexPotentialsSharedModule {}
