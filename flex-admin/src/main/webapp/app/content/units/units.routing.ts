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
      screen: Screen.ADMIN_UNITS,
    },
    children: [
      {
        path: 'list',
        data: {
          screen: Screen.ADMIN_UNITS,
        },
        resolve: {
          viewConfiguration: ViewConfigurationResolver,
        },
        component: UnitsListComponent,
      },
      {
        path: 'self-schedules',
        component: UnitsSelfSchedulesComponent,
        data: {
          screen: Screen.ADMIN_SELF_SCHEDULE,
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
export class UnitsRoutingModule {}
