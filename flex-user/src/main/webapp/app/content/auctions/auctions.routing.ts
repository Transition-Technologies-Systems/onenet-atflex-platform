import { RouterModule, Routes } from '@angular/router';
import { TranslateResolver, UserRouteAccessService } from '@app/core';

import { AuctionType } from './enums';
import { AuctionsComponent } from './auctions.component';
import { CmVcComponent } from './cm-vc';
import { DayAheadComponent } from './day-ahead';
import { NgModule } from '@angular/core';
import { ViewConfigurationResolver } from '@app/shared/commons/view-configuration';

const routes: Routes = [
  {
    path: '',
    component: AuctionsComponent,
    data: {
      i18n: ['auctions', 'products', 'flex-potentials', 'scheduling-units', 'units'],
      pageTitle: 'global.title',
    },
    resolve: {
      translation: TranslateResolver,
    },
    children: [
      {
        path: 'cm-vc',
        component: CmVcComponent,
        data: {
          type: AuctionType.CMVC,
          authorities: ['FLEX_USER_AUCTIONS_CMVC_VIEW'],
        },
        canActivate: [UserRouteAccessService],
        resolve: {
          viewConfiguration: ViewConfigurationResolver,
        },
      },
      {
        path: 'day-ahead',
        component: DayAheadComponent,
        data: {
          type: AuctionType.DAY_AHEAD,
          authorities: ['FLEX_USER_AUCTIONS_DAY_AHEAD_VIEW'],
        },
        canActivate: [UserRouteAccessService],
        resolve: {
          viewConfiguration: ViewConfigurationResolver,
        },
      },
      {
        path: 'day-ahead/energy',
        component: DayAheadComponent,
        data: {
          type: AuctionType.DAY_AHEAD,
          activeTab: 'energy-auctions',
          authorities: ['FLEX_USER_AUCTIONS_DAY_AHEAD_VIEW'],
        },
        canActivate: [UserRouteAccessService],
        resolve: {
          viewConfiguration: ViewConfigurationResolver,
        },
      },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class AuctionsRoutingModule {}
