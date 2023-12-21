import { NgModule } from '@angular/core';
import { Route, RouterModule } from '@angular/router';
import { TranslateResolver } from '@app/core';

import { LoginComponent } from './login.component';

const route: Route = {
  path: '',
  component: LoginComponent,
  data: {
    i18n: ['login'],
    pageTitle: 'global.title',
  },
  resolve: {
    translation: TranslateResolver,
  },
};

@NgModule({
  imports: [RouterModule.forChild([route])],
  exports: [RouterModule],
})
export class LoginRoutingModule {}
