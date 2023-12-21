import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { HasKeyParamGuard, TranslateResolver } from '@app/core';

import { ResetPasswordComponent } from './reset-password';

const routes: Routes = [
  {
    path: 'reset/finish',
    component: ResetPasswordComponent,
    canActivate: [HasKeyParamGuard],
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
