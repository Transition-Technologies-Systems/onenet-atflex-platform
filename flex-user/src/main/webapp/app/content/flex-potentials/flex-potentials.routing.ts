import { Role, Screen } from '@app/shared/enums';
import { RouterModule, Routes } from '@angular/router';
import { TranslateResolver, UserRouteAccessService } from '@app/core';

import { FlexPotentialsComponent } from './flex-potentials.component';
import { NgModule } from '@angular/core';
import { ViewConfigurationResolver } from '@app/shared/commons/view-configuration';

const routes: Routes = [
  {
    path: '',
    component: FlexPotentialsComponent,
    data: {
      screen: Screen.USER_FLEXIBILITY_POTENTIALS,
      i18n: ['flex-potentials', 'products'],
      pageTitle: 'global.title',
    },
    resolve: {
      translation: TranslateResolver,
      viewConfiguration: ViewConfigurationResolver,
    },
  },
  {
    path: 'register',
    component: FlexPotentialsComponent,
    data: {
      roles: [Role.ROLE_FLEX_SERVICE_PROVIDER, Role.ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED],
      screen: Screen.USER_REGISTER_FLEXIBILITY_POTENTIALS,
      i18n: ['flex-potentials', 'products'],
      pageTitle: 'global.title',
      type: 'REGISTER',
    },
    canActivate: [UserRouteAccessService],
    resolve: {
      translation: TranslateResolver,
      viewConfiguration: ViewConfigurationResolver,
    },
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class FlexPotentialsRoutingModule {}
