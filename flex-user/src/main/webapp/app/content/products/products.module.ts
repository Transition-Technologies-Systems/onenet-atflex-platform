import { AccordionModule } from 'primeng/accordion';

import { NgModule } from '@angular/core';
import { SharedModule } from '@app/shared';

import { ProductsFiltersComponent, ProductsFiltersService } from './filters';
import { ProductsPreviewComponent } from './preview';
import { ProductsComponent } from './products.component';
import { ProductsRoutingModule } from './products.routing';
import { ProductsService } from './products.service';
import { ProductsStore } from './products.store';
import { ProductsSharedModule } from './shared';

@NgModule({
  imports: [SharedModule, ProductsRoutingModule, AccordionModule, ProductsSharedModule],
  declarations: [ProductsComponent, ProductsFiltersComponent, ProductsPreviewComponent],
  providers: [ProductsService, ProductsStore, ProductsFiltersService],
})
export class ProductsModule {}
