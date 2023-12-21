import { RouterModule, Routes } from '@angular/router';

import { NgModule } from '@angular/core';
import { ProposalConfirmComponent } from '@app/shared/proposal/confirm';
import { SchedulingUnitsComponent } from './scheduling-units.component';
import { Screen } from '@app/shared/enums';
import { TranslateResolver } from '@app/core';
import { ViewConfigurationResolver } from '@app/shared/commons/view-configuration';

const routes: Routes = [
  {
    path: '',
    component: SchedulingUnitsComponent,
    data: {
      i18n: ['scheduling-units', 'products', 'units'],
      pageTitle: 'global.title',
      screen: Screen.ADMIN_SCHEDULING_UNITS,
    },
    resolve: {
      translation: TranslateResolver,
      viewConfiguration: ViewConfigurationResolver,
    },
  },
  {
    path: 'register',
    component: SchedulingUnitsComponent,
    data: {
      i18n: ['scheduling-units', 'products', 'units'],
      pageTitle: 'global.title',
      screen: Screen.ADMIN_REGISTER_SCHEDULING_UNITS,
      type: 'REGISTER',
    },
    resolve: {
      translation: TranslateResolver,
      viewConfiguration: ViewConfigurationResolver,
    },
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
