import { RouterModule, Routes } from '@angular/router';

import { FspsComponent } from './fsps.component';
import { NgModule } from '@angular/core';
import { Screen } from '@app/shared/enums';
import { TranslateResolver } from '@app/core';
import { ViewConfigurationResolver } from '@app/shared/commons/view-configuration';

const routes: Routes = [
  {
    path: '',
    component: FspsComponent,
    data: {
      i18n: ['fsps'],
      pageTitle: 'global.title',
      screen: Screen.USER_BSP,
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
export class FspsRoutingModule {}
