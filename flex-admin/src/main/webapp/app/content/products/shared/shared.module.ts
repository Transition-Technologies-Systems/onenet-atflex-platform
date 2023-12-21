import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProductPreviewComponent } from './products-preview/product-preview.component';
import { AccordionModule } from 'primeng/accordion';
import { TranslateModule } from '@ngx-translate/core';
import { SharedCommonsModule } from '@app/shared/commons/shared-commons.module';

@NgModule({
  declarations: [ProductPreviewComponent],
  imports: [CommonModule, AccordionModule, TranslateModule, SharedCommonsModule],
  exports: [ProductPreviewComponent, TranslateModule],
})
export class ProductsSharedModule {}
