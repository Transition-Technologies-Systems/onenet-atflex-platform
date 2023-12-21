import { RouterModule, Routes } from '@angular/router';
import { ActivationsSettlementsComponent } from './activations-settlements.component';
import { Screen } from '@app/shared/enums';
import { TranslateResolver } from '@app/core';
import { ViewConfigurationResolver } from '@app/shared/commons/view-configuration';
import { NgModule } from '@angular/core';

const routes: Routes = [
  {
    path: '',
    component: ActivationsSettlementsComponent,
    data: {
      i18n: ['activations-settlements'],
      pageTitle: 'global.title',
      screen: Screen.ADMIN_SETTLEMENT,
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
export class ActivationsSettlementsRoutingModule {}
