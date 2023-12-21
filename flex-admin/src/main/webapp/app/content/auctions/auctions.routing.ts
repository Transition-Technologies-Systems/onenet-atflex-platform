import { RouterModule, Routes } from '@angular/router';
import { TranslateResolver, UserRouteAccessService } from '@app/core';

import { AlgorithmEvaluationsComponent } from './algorithm-evaluations';
import { AuctionBidComponent } from './bid-dialog';
import { AuctionType } from './enums';
import { AuctionsComponent } from './auctions.component';
import { BidsEvaluationComponent } from './bids-evaluation';
import { CmVcComponent } from './cm-vc';
import { DayAheadComponent } from './day-ahead';
import { NgModule } from '@angular/core';
import { Screen } from '@app/shared/enums';
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
        path: 'algorithm-evaluations',
        component: AlgorithmEvaluationsComponent,
        data: {
          authorities: ['FLEX_ADMIN_ALGORITHM_EVALUATIONS'],
        },
        canActivate: [UserRouteAccessService],
      },
      {
        path: 'bids-evaluation',
        component: BidsEvaluationComponent,
        data: {
          authorities: ['FLEX_ADMIN_AUCTIONS_OFFER_VIEW'],
          screen: Screen.ADMIN_BIDS_EVALUATION,
        },
        canActivate: [UserRouteAccessService],
        resolve: {
          viewConfiguration: ViewConfigurationResolver,
        },
      },
      {
        path: 'cm-vc',
        component: CmVcComponent,
        data: {
          type: AuctionType.CMVC,
          authorities: ['FLEX_ADMIN_AUCTIONS_CMVC_VIEW'],
        },
        canActivate: [UserRouteAccessService],
        resolve: {
          viewConfiguration: ViewConfigurationResolver,
        },
        children: [
          {
            path: 'offer/:auctionId',
            component: AuctionBidComponent,
            data: {
              type: AuctionType.CMVC,
            },
          },
        ],
      },
      {
        path: 'day-ahead',
        component: DayAheadComponent,
        data: {
          type: AuctionType.DAY_AHEAD,
          authorities: ['FLEX_ADMIN_AUCTIONS_DAY_AHEAD_VIEW'],
        },
        canActivate: [UserRouteAccessService],
        resolve: {
          viewConfiguration: ViewConfigurationResolver,
        },
      },
      {
        path: 'offer/day-ahead/:auctionId',
        component: AuctionBidComponent,
        data: {
          type: AuctionType.DAY_AHEAD,
        },
      },
      {
        path: 'offer/day-ahead/:auctionId/:bidId',
        component: AuctionBidComponent,
        data: {
          type: AuctionType.DAY_AHEAD,
        },
      },
      {
        path: 'offer/cmvc/:auctionId',
        component: AuctionBidComponent,
        data: {
          type: AuctionType.DAY_AHEAD,
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
