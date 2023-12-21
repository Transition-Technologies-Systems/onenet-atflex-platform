import { NgModule } from '@angular/core';
import { Route, RouterModule } from '@angular/router';

import { LoginComponent } from './login.component';

const route: Route = {
  path: '',
  component: LoginComponent,
  data: {
    pageTitle: 'global.title',
  },
};

@NgModule({
  imports: [RouterModule.forChild([route])],
  exports: [RouterModule],
})
export class LoginRoutingModule {}
