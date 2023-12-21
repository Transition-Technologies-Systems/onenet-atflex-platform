import { RouterModule, Routes } from '@angular/router';
import { TranslateResolver, UserRouteAccessService } from '@app/core';
import { NgModule } from '@angular/core';
import { OneNetSystemComponent } from './onenet-system.component';
import { OnsUsersComponent } from './ons-users/ons-users.component';
import { ConsumeDataComponent } from './consume-data/consume-data.component';
import { OfferedServicesComponent } from './offered-services/offered-services.component';
import { ProvideDataComponent } from './provide-data/provide-data.component';
import { ViewConfigurationResolver } from '@app/shared/commons/view-configuration';
import { Screen } from '@app/shared/enums';

const routes: Routes = [
  {
    path: '',
    component: OneNetSystemComponent,
    data: {
      pageTitle: 'global.title',
    },
    children: [
      {
        path: 'ons-users',
        component: OnsUsersComponent,
        data: {
          i18n: ['onenet-system/ons-users'],
          authorities: ['FLEX_ADMIN_ONENET_USER_VIEW'],
          screen: Screen.ADMIN_ONENET_USER,
        },
        canActivate: [UserRouteAccessService],
        resolve: {
          translation: TranslateResolver,
          viewConfiguration: ViewConfigurationResolver,
        },
      },
      {
        path: 'consume-data',
        component: ConsumeDataComponent,
        data: {
          i18n: ['onenet-system/consume-data'],
          authorities: ['FLEX_ADMIN_CONSUME_DATA_VIEW'],
          screen: Screen.ADMIN_CONSUME_DATA,
        },
        canActivate: [UserRouteAccessService],
        resolve: {
          translation: TranslateResolver,
          viewConfiguration: ViewConfigurationResolver,
        },
      },
      {
        path: 'offered-services',
        component: OfferedServicesComponent,
        data: {
          i18n: ['onenet-system/offered-services', 'onenet-system/provide-dialog'],
          authorities: ['FLEX_ADMIN_OFFERED_SERVICES_VIEW'],
          screen: Screen.ADMIN_OFFERED_SERVICES,
        },
        canActivate: [UserRouteAccessService],
        resolve: {
          translation: TranslateResolver,
          viewConfiguration: ViewConfigurationResolver,
        },
      },
      {
        path: 'provide-data',
        component: ProvideDataComponent,
        data: {
          i18n: ['onenet-system/provide-data', 'onenet-system/provide-dialog'],
          authorities: ['FLEX_ADMIN_PROVIDE_DATA_VIEW'],
          screen: Screen.ADMIN_PROVIDE_DATA,
        },
        canActivate: [UserRouteAccessService],
        resolve: {
          translation: TranslateResolver,
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
export class OneNetSystemRoutingModule {}
