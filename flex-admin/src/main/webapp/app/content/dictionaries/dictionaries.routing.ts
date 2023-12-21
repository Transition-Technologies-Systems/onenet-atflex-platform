import { DictionaryType, Screen } from '@app/shared/enums';
import { RouterModule, Routes } from '@angular/router';

import { DictionariesComponent } from './dictionaries.component';
import { NgModule } from '@angular/core';
import { ViewConfigurationResolver } from '@app/shared/commons/view-configuration';
import { UserRouteAccessService } from '@app/core';

const routes: Routes = [
  {
    path: '',
    component: DictionariesComponent,
  },
  {
    path: 'der-type',
    component: DictionariesComponent,
    data: {
      screen: Screen.ADMIN_DER_TYPES,
      type: DictionaryType.DER_TYPE,
      authorities: ['FLEX_ADMIN_DER_TYPE_VIEW'],
    },
    resolve: {
      viewConfiguration: ViewConfigurationResolver,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'su-type',
    component: DictionariesComponent,
    data: {
      screen: Screen.ADMIN_SCHEDULING_UNIT_TYPE,
      type: DictionaryType.SCHEDULING_UNIT_TYPE,
      authorities: ['FLEX_ADMIN_SCHEDULING_UNIT_TYPE_VIEW'],
    },
    resolve: {
      viewConfiguration: ViewConfigurationResolver,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'localization-type',
    component: DictionariesComponent,
    data: {
      screen: Screen.ADMIN_LOCALIZATION_TYPE,
      type: DictionaryType.LOCALIZATION_TYPE,
      authorities: ['FLEX_ADMIN_LOCALIZATION_TYPE_VIEW'],
    },
    resolve: {
      viewConfiguration: ViewConfigurationResolver,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'kdm-models',
    component: DictionariesComponent,
    data: {
      screen: Screen.ADMIN_KDM_MODEL,
      type: DictionaryType.KDM_MODEL,
      authorities: ['FLEX_ADMIN_KDM_MODEL_VIEW'],
    },
    resolve: {
      viewConfiguration: ViewConfigurationResolver,
    },
    canActivate: [UserRouteAccessService],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class DictionariesRoutingModule {}
