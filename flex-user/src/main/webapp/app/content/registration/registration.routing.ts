import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { HasKeyParamGuard } from '@app/core/auth/guard/has-key-param.guard';

import { RegistrationConfirmComponent } from './confirm';
import { RegistrationComponent } from './registration.component';
import { RegistrationRejectComponent } from './reject';

const routes: Routes = [
  {
    path: '',
    component: RegistrationComponent,
  },
  {
    path: 'confirm',
    canActivate: [HasKeyParamGuard],
    component: RegistrationConfirmComponent,
  },
  {
    path: 'reject',
    canActivate: [HasKeyParamGuard],
    component: RegistrationRejectComponent,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class RegistrationRoutingModule {}
