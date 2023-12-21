import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { TranslateResolver } from '@app/core';
import { ViewConfigurationResolver } from '@app/shared/commons/view-configuration';
import { Screen } from '@app/shared/enums';

import { SubportfoliosComponent } from './subportfolio.component';

const routes: Routes = [
  {
    path: '',
    component: SubportfoliosComponent,
    data: {
      i18n: ['subportfolio'],
      pageTitle: 'global.title',
      screen: Screen.USER_SUBPORTFOLIO,
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
export class SubportfoliosRoutingModule {}
