import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { TranslateResolver } from '@app/core';
import { ViewConfigurationResolver } from '@app/shared/commons/view-configuration';
import { Screen } from '@app/shared/enums';

import { UsersComponent } from './users.component';

const routes: Routes = [
  {
    path: '',
    component: UsersComponent,
    data: {
      i18n: ['users'],
      pageTitle: 'global.title',
      screen: Screen.ADMIN_USERS,
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
export class UsersRoutingModule {}
