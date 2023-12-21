import { RouterModule, Routes } from '@angular/router';
import { TranslateResolver, UserRouteAccessService } from '@app/core';

import { NgModule } from '@angular/core';
import { ProductsComponent } from './products.component';
import { ProductsForecastedPricesComponent } from './tabs/forecasted-prices';
import { ProductsListComponent } from './tabs/list';
import { Screen } from '@app/shared/enums';
import { ViewConfigurationResolver } from '@app/shared/commons/view-configuration';

const routes: Routes = [
  {
    path: '',
    redirectTo: 'list',
    pathMatch: 'full',
  },
  {
    path: '',
    component: ProductsComponent,
    data: {
      i18n: ['products'],
      pageTitle: 'global.title',
      screen: Screen.ADMIN_PRODUCTS,
    },
    children: [
      {
        path: 'list',
        data: {
          screen: Screen.ADMIN_PRODUCTS,
        },
        resolve: {
          viewConfiguration: ViewConfigurationResolver,
        },
        component: ProductsListComponent,
      },
      {
        path: 'forecasted-prices',
        component: ProductsForecastedPricesComponent,
        data: {
          screen: Screen.ADMIN_FORECASTED_PRICES,
          authorities: ['FLEX_ADMIN_SELF_SCHEDULE_VIEW'],
        },
        resolve: {
          viewConfiguration: ViewConfigurationResolver,
        },
        canActivate: [UserRouteAccessService],
      },
    ],
    resolve: {
      translation: TranslateResolver,
    },
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class ProductsRoutingModule {}
