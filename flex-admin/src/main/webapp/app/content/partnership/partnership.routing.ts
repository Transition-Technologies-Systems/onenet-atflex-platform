import { RouterModule, Routes } from '@angular/router';

import { NgModule } from '@angular/core';
import { PartnershipComponent } from './partnership.component';
import { Screen } from '@app/shared/enums';
import { TranslateResolver } from '@app/core';
import { ViewConfigurationResolver } from '@app/shared/commons/view-configuration';

const routes: Routes = [
  {
    path: '',
    component: PartnershipComponent,
    data: {
      i18n: ['partnership'],
      pageTitle: 'global.title',
      screen: Screen.ADMIN_PARTNERSHIP,
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
export class PartnershipRoutingModule {}
