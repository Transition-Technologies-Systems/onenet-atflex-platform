import { ProductsFiltersComponent, ProductsFiltersService } from './filters';
import { ProductsForecastedPricesComponent, ProductsForecastedPricessService } from './tabs/forecasted-prices';

import { AccordionModule } from 'primeng/accordion';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { NgModule } from '@angular/core';
import { ProductsComponent } from './products.component';
import { ProductsDialogComponent } from './dialog';
import { ProductsDialogService } from './dialog/dialog.service';
import { ProductsForecastedPricesPreviewComponent } from './tabs/forecasted-prices/preview';
import { ProductsListComponent } from './tabs/list';
import { ProductsPreviewComponent } from './preview';
import { ProductsRoutingModule } from './products.routing';
import { ProductsService } from './products.service';
import { ProductsSharedModule } from './shared';
import { SelectButtonModule } from 'primeng/selectbutton';
import { SharedModule } from '@app/shared';

@NgModule({
  imports: [SharedModule, ProductsRoutingModule, AccordionModule, ProductsSharedModule, SelectButtonModule, ConfirmDialogModule],
  declarations: [
    ProductsComponent,
    ProductsDialogComponent,
    ProductsFiltersComponent,
    ProductsPreviewComponent,
    ProductsListComponent,
    ProductsForecastedPricesComponent,
    ProductsForecastedPricesPreviewComponent,
  ],
  providers: [ProductsService, ProductsDialogService, ProductsFiltersService, ProductsForecastedPricessService],
})
export class ProductsModule {}
