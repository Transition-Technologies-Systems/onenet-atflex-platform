import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { TranslateResolver } from '@app/core';

import { RegistrationThreadComponent } from './registration-thread.component';
import { RegistrationThreadResolver } from './registration-thread.resolver';

const routes: Routes = [
  {
    path: '',
    component: RegistrationThreadComponent,
    data: {
      i18n: ['registration-thread'],
      pageTitle: 'global.title',
    },
    resolve: {
      translation: TranslateResolver,
      fspUserRegistration: RegistrationThreadResolver,
    },
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class RegistrationThreadRoutingModule {}
