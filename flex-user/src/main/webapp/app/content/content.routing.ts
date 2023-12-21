import { TranslateResolver, UserRouteAccessService } from '@app/core';

import { Routes } from '@angular/router';

export const CONTENT_ROUTES: Routes = [
  {
    path: 'auctions',
    data: {
      anyAuthorities: ['FLEX_USER_AUCTIONS_CMVC_VIEW', 'FLEX_USER_AUCTIONS_SERIES_VIEW'],
    },
    canActivate: [UserRouteAccessService],
    loadChildren: () => import('./auctions/auctions.module').then(m => m.AuctionsModule),
  },
  {
    path: 'activations-settlements',
    data: {
      authorities: ['FLEX_USER_SETTLEMENT_VIEW'],
    },
    canActivate: [UserRouteAccessService],
    loadChildren: () => import('./activations-settlements/activations-settlements.module').then(m => m.ActivationsSettlementsModule),
  },
  {
    path: 'chat',
    data: {
      authorities: ['FLEX_USER_CHAT_VIEW'],
      containerCustomClass: 'p-0',
    },
    canActivate: [UserRouteAccessService],
    loadChildren: () => import('./chat/chat.module').then(m => m.ChatModule),
  },
  {
    path: 'flexibility-potentials',
    data: {
      authorities: ['FLEX_USER_FP_VIEW'],
    },
    canActivate: [UserRouteAccessService],
    loadChildren: () => import('./flex-potentials/flex-potentials.module').then(m => m.FlexPotentialsModule),
  },
  {
    path: 'subportfolios',
    data: {
      authorities: ['FLEX_USER_SUBPORTFOLIO_VIEW'],
    },
    canActivate: [UserRouteAccessService],
    loadChildren: () => import('./subportfolio/subportfolio.module').then(m => m.SubportfoliosModule),
  },
  {
    path: 'partnership',
    data: {
      authorities: ['FLEX_USER_SCHEDULING_UNIT_PROPOSAL_VIEW'],
    },
    canActivate: [UserRouteAccessService],
    loadChildren: () => import('./partnership/partnership.module').then(m => m.PartnershipModule),
  },
  {
    path: 'products',
    data: {
      authorities: ['FLEX_USER_PRODUCT_VIEW'],
    },
    canActivate: [UserRouteAccessService],
    loadChildren: () => import('./products/products.module').then(m => m.ProductsModule),
  },
  {
    path: 'registration-thread',
    data: {
      authorities: ['FLEX_USER_FSP_REGISTRATION_VIEW'],
    },
    canActivate: [UserRouteAccessService],
    loadChildren: () => import('./registration-thread/registration-thread.module').then(m => m.RegistrationThreadModule),
  },
  {
    path: 'scheduling-units',
    data: {
      authorities: ['FLEX_USER_SCHEDULING_UNIT_VIEW'],
    },
    canActivate: [UserRouteAccessService],
    loadChildren: () => import('./scheduling-units/scheduling-units.module').then(m => m.SchedulingUnitsModule),
  },
  {
    path: 'ders',
    data: {
      authorities: ['FLEX_USER_UNIT_VIEW'],
    },
    canActivate: [UserRouteAccessService],
    loadChildren: () => import('./units/units.module').then(m => m.UnitsModule),
  },
  {
    path: 'bsps',
    data: {
      authorities: ['FLEX_USER_BSP_VIEW'],
    },
    canActivate: [UserRouteAccessService],
    loadChildren: () => import('./fsps/fsps.module').then(m => m.FspsModule),
  },
  {
    path: 'user-profile',
    resolve: {
      translation: TranslateResolver,
    },
    canActivate: [UserRouteAccessService],
    loadChildren: () => import('./user-profile/user-profile.module').then(m => m.UserProfileModule),
  },
];
