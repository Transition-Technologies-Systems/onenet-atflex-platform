import { Role, Screen } from '@app/shared/enums';
import { RouterModule, Routes } from '@angular/router';
import { TranslateResolver, UserRouteAccessService } from '@app/core';

import { NgModule } from '@angular/core';
import { ProposalConfirmComponent } from '@app/shared/proposal/confirm';
import { SchedulingUnitsComponent } from './scheduling-units.component';
import { SchedulingUnitsListComponent } from './tabs/list';
import { SchedulingUnitsTypesComponent } from './tabs/types-su';
import { ViewConfigurationResolver } from '@app/shared/commons/view-configuration';

const routes: Routes = [
  {
    path: '',
    component: SchedulingUnitsComponent,
    data: {
      i18n: ['scheduling-units', 'products', 'units'],
      pageTitle: 'global.title',
      screen: Screen.USER_SCHEDULING_UNITS,
    },
    resolve: {
      translation: TranslateResolver,
      viewConfiguration: ViewConfigurationResolver,
    },
    children: [
      {
        path: '',
        component: SchedulingUnitsListComponent,
      },
      {
        path: 'types',
        data: {
          roles: [Role.ROLE_BALANCING_SERVICE_PROVIDER],
        },
        canActivate: [UserRouteAccessService],
        component: SchedulingUnitsTypesComponent,
      },
    ],
  },
  {
    path: 'register',
    component: SchedulingUnitsComponent,
    data: {
      roles: [Role.ROLE_BALANCING_SERVICE_PROVIDER],
      i18n: ['scheduling-units', 'products', 'units'],
      pageTitle: 'global.title',
      screen: Screen.USER_REGISTER_SCHEDULING_UNITS,
      type: 'REGISTER',
    },
    canActivate: [UserRouteAccessService],
    resolve: {
      translation: TranslateResolver,
      viewConfiguration: ViewConfigurationResolver,
    },
    children: [
      {
        path: '',
        component: SchedulingUnitsListComponent,
      },
    ],
  },
  {
    path: 'unitProposalLink',
    data: {
      i18n: ['scheduling-units', 'products'],
    },
    component: ProposalConfirmComponent,
    resolve: {
      translation: TranslateResolver,
    },
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class SchedulingUnitsRoutingModule {}
