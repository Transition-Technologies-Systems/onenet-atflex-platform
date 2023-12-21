import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { TranslateResolver } from '@app/core';

import { ChangePasswordComponent } from './change-password';

const routes: Routes = [
  {
    path: 'change-password',
    component: ChangePasswordComponent,
    data: {
      i18n: ['account'],
      pageTitle: 'global.title',
    },
    resolve: {
      translation: TranslateResolver,
    },
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class AccountRoutingModule {}
