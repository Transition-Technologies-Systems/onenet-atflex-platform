import { RouterModule, Routes } from '@angular/router';
import { TranslateResolver, UserRouteAccessService } from '@app/core';

import { NgModule } from '@angular/core';
import { Screen } from '@app/shared/enums';
import { UnitsComponent } from './units.component';
import { UnitsListComponent } from './tabs/list';
import { UnitsSelfSchedulesComponent } from './tabs/self-schedules';
import { ViewConfigurationResolver } from '@app/shared/commons/view-configuration';

const routes: Routes = [
  {
    path: '',
    redirectTo: 'list',
    pathMatch: 'full',
  },
  {
    path: '',
    component: UnitsComponent,
    data: {
      i18n: ['units'],
      pageTitle: 'global.title',
      screen: Screen.USER_SELF_SCHEDULE,
    },
    children: [
      {
        path: 'list',
        component: UnitsListComponent,
        data: {
          screen: Screen.USER_UNITS,
        },
        resolve: {
          viewConfiguration: ViewConfigurationResolver,
        },
      },
      {
        path: 'self-schedules',
        component: UnitsSelfSchedulesComponent,
        data: {
          authorities: ['FLEX_USER_SELF_SCHEDULE_VIEW'],
          screen: Screen.USER_SELF_SCHEDULE,
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
export class UnitsRoutingModule {}
