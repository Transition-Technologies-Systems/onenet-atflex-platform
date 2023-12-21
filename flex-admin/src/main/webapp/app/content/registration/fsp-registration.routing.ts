import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { TranslateResolver } from '@app/core';
import { ViewConfigurationResolver } from '@app/shared/commons/view-configuration';
import { Screen } from '@app/shared/enums';

import { FspRegistrationComponent } from './fsp-registration.component';

const routes: Routes = [
  {
    path: '',
    component: FspRegistrationComponent,
    data: {
      i18n: ['users/fsp-registration'],
      pageTitle: 'global.title',
      screen: Screen.ADMIN_REGISTRATION,
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
export class FspRegistrationRoutingModule {}
