import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { TranslateResolver } from '@app/core';
import { ViewConfigurationResolver } from '@app/shared/commons/view-configuration';
import { Screen } from '@app/shared/enums';

import { FlexPotentialsComponent } from './flex-potentials.component';

const routes: Routes = [
  {
    path: '',
    component: FlexPotentialsComponent,
    data: {
      screen: Screen.ADMIN_FLEXIBILITY_POTENTIALS,
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
      screen: Screen.ADMIN_FLEX_REGISTER,
      i18n: ['flex-potentials', 'products'],
      pageTitle: 'global.title',
      type: 'REGISTER',
    },
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
