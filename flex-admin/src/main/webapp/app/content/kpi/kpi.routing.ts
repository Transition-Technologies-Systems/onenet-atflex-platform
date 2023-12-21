import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { TranslateResolver } from '@app/core';
import { ViewConfigurationResolver } from '@app/shared/commons/view-configuration';
import { Screen } from '@app/shared/enums';
import { KpiComponent } from './kpi.component';

const routes: Routes = [
  {
    path: '',
    component: KpiComponent,
    data: {
      i18n: ['kpi'],
      pageTitle: 'global.title',
      screen: Screen.ADMIN_KPI,
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
export class KpiRoutingModule {}
