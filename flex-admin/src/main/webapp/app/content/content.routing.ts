import { TranslateResolver, UserRouteAccessService } from '@app/core';

import { Role } from '@app/shared/enums';
import { Routes } from '@angular/router';
import { Screen } from '@app/shared/enums';

export const CONTENT_ROUTES: Routes = [
  {
    path: 'auctions',
    data: {
      anyAuthorities: ['FLEX_ADMIN_AUCTIONS_OFFER_VIEW', 'FLEX_ADMIN_AUCTIONS_CMVC_VIEW', 'FLEX_ADMIN_AUCTIONS_SERIES_VIEW'],
    },
    canActivate: [UserRouteAccessService],
    loadChildren: () => import('./auctions/auctions.module').then(m => m.AuctionsModule),
  },
  {
    path: 'activations-settlements',
    data: {
      authorities: ['FLEX_ADMIN_SETTLEMENT_VIEW'],
    },
    canActivate: [UserRouteAccessService],
    loadChildren: () => import('./activations-settlements/activations-settlements.module').then(m => m.ActivationsSettlementsModule),
  },
  {
    path: 'chat',
    data: {
      authorities: ['FLEX_ADMIN_CHAT_VIEW'],
      containerCustomClass: 'p-0',
    },
    canActivate: [UserRouteAccessService],
    loadChildren: () => import('./chat/chat.module').then(m => m.ChatModule),
  },
  {
    path: 'dictionaries',
    data: {
      i18n: ['dictionaries-page'],
    },
    resolve: {
      translation: TranslateResolver,
    },
    canActivate: [UserRouteAccessService],
    loadChildren: () => import('./dictionaries/dictionaries.module').then(m => m.DictionariesModule),
  },
  {
    path: 'flexibility-potentials',
    data: {
      authorities: ['FLEX_ADMIN_FP_VIEW'],
    },
    canActivate: [UserRouteAccessService],
    loadChildren: () => import('./flex-potentials/flex-potentials.module').then(m => m.FlexPotentialsModule),
  },
  {
    path: 'fsps',
    data: {
      authorities: ['FLEX_ADMIN_FSP_VIEW'],
      screen: Screen.ADMIN_FSP,
    },
    canActivate: [UserRouteAccessService],
    loadChildren: () => import('./fsps/fsps.module').then(m => m.FspsModule),
  },
  {
    path: 'bsps',
    data: {
      authorities: ['FLEX_ADMIN_FSP_VIEW'],
      role: Role.ROLE_BALANCING_SERVICE_PROVIDER,
      screen: Screen.ADMIN_BSP,
    },
    canActivate: [UserRouteAccessService],
    loadChildren: () => import('./fsps/fsps.module').then(m => m.FspsModule),
  },
  {
    path: 'subportfolios',
    data: {
      authorities: ['FLEX_ADMIN_SUBPORTFOLIO_VIEW'],
    },
    canActivate: [UserRouteAccessService],
    loadChildren: () => import('./subportfolio/subportfolio.module').then(m => m.SubportfoliosModule),
  },
  {
    path: 'products',
    data: {
      authorities: ['FLEX_ADMIN_PRODUCT_VIEW'],
    },
    canActivate: [UserRouteAccessService],
    loadChildren: () => import('./products/products.module').then(m => m.ProductsModule),
  },
  {
    path: 'registration',
    data: {
      authorities: ['FLEX_ADMIN_FSP_REGISTRATION_VIEW'],
    },
    canActivate: [UserRouteAccessService],
    loadChildren: () => import('./registration/fsp-registration.module').then(m => m.FspRegistrationModule),
  },
  {
    path: 'partnership',
    data: {
      authorities: ['FLEX_ADMIN_SCHEDULING_UNIT_PROPOSAL_VIEW'],
    },
    canActivate: [UserRouteAccessService],
    loadChildren: () => import('./partnership/partnership.module').then(m => m.PartnershipModule),
  },
  {
    path: 'scheduling-units',
    data: {
      authorities: ['FLEX_ADMIN_SCHEDULING_UNIT_VIEW'],
    },
    canActivate: [UserRouteAccessService],
    loadChildren: () => import('./scheduling-units/scheduling-units.module').then(m => m.SchedulingUnitsModule),
  },
  {
    path: 'ders',
    data: {
      authorities: ['FLEX_ADMIN_UNIT_VIEW'],
    },
    canActivate: [UserRouteAccessService],
    loadChildren: () => import('./units/units.module').then(m => m.UnitsModule),
  },
  {
    path: 'users',
    data: {
      authorities: ['FLEX_ADMIN_USER_VIEW'],
    },
    canActivate: [UserRouteAccessService],
    loadChildren: () => import('./users/users.module').then(m => m.UsersModule),
  },
  {
    path: 'user-profile',
    resolve: {
      translation: TranslateResolver,
    },
    canActivate: [UserRouteAccessService],
    loadChildren: () => import('./user-profile/user-profile.module').then(m => m.UserProfileModule),
  },
  {
    path: 'onenet-system',
    canActivate: [UserRouteAccessService],
    loadChildren: () => import('./onenet-system/onenet-system.module').then(m => m.OneNetSystemModule),
  },
  {
    path: 'kpi',
    data: {
      authorities: ['FLEX_ADMIN_KPI_VIEW'],
    },
    canActivate: [UserRouteAccessService],
    loadChildren: () => import('./kpi/kpi.module').then(m => m.KpiModule),
  },
];
