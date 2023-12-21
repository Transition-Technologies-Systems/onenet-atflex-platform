import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { HasKeyParamGuard } from '@app/core/auth/guard/has-key-param.guard';

import { ActivateAccountComponent } from './activate';
import { ResetPasswordComponent } from './reset-password';

const routes: Routes = [
  {
    path: 'activate',
    canActivate: [HasKeyParamGuard],
    component: ActivateAccountComponent,
  },
  {
    path: 'reset/finish',
    canActivate: [HasKeyParamGuard],
    component: ResetPasswordComponent,
    data: {
      pageTitle: 'global.title',
    },
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class AccountRoutingModule {}
