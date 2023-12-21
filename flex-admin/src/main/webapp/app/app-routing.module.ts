import { CONTENT_ROUTES, LoggedMainPageComponent, UnloggedMainPageComponent } from './content';
import { IsAuthenticatedGuard, IsNotAuthenticatedGuard } from './core';
import { RouterModule, Routes } from '@angular/router';

import { NgModule } from '@angular/core';

const routes: Routes = [
  {
    path: '',
    component: LoggedMainPageComponent,
    canMatch: [IsAuthenticatedGuard],
    children: [
      ...CONTENT_ROUTES,
      {
        path: 'account',
        loadChildren: () => import('./content/account/logged/account.module').then(m => m.AccountModule),
      },
      {
        path: '**',
        redirectTo: '/',
      },
    ],
  },
  {
    path: '',
    component: UnloggedMainPageComponent,
    canMatch: [IsNotAuthenticatedGuard],
    children: [
      {
        path: 'login',
        loadChildren: () => import('./content/login/login.module').then(m => m.LoginModule),
      },
      {
        path: 'account',
        loadChildren: () => import('./content/account/unlogged/account.module').then(m => m.AccountModule),
      },
      {
        path: '**',
        redirectTo: '/login',
      },
    ],
  },
];

@NgModule({
  imports: [
    RouterModule.forRoot(routes, {
      useHash: false,
      enableTracing: false,
      scrollPositionRestoration: 'top',
      relativeLinkResolution: 'legacy',
    }),
  ],
  exports: [RouterModule],
})
export class AppRoutingModule {}
