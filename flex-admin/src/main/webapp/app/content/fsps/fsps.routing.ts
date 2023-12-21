import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { TranslateResolver } from '@app/core';
import { ViewConfigurationResolver } from '@app/shared/commons/view-configuration';

import { FspsComponent } from './fsps.component';

const routes: Routes = [
  {
    path: '',
    component: FspsComponent,
    data: {
      i18n: ['fsps'],
      pageTitle: 'global.title',
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
