import { CONTENT_ROUTES, LoggedMainPageComponent, UnloggedMainPageComponent } from './content';
import { IsAuthenticatedGuard, IsNotAuthenticatedGuard, TranslateResolver } from './core';
import { RouterModule, Routes } from '@angular/router';

import { IsRegistrationUserGuard } from './is-registration-user.guard';
import { NgModule } from '@angular/core';

const routes: Routes = [
  {
    path: '',
    component: LoggedMainPageComponent,
    canMatch: [IsAuthenticatedGuard],
    canActivate: [IsRegistrationUserGuard],
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
        path: 'account',
        data: {
          i18n: ['account'],
        },
        loadChildren: () => import('./content/account/unlogged/account.module').then(m => m.AccountModule),
        resolve: {
          translation: TranslateResolver,
        },
      },
      {
        path: 'login',
        data: {
          i18n: ['login'],
        },
        loadChildren: () => import('./content/login/login.module').then(m => m.LoginModule),
        resolve: {
          translation: TranslateResolver,
        },
      },
      {
        path: 'registration',
        data: {
          i18n: ['registration'],
        },
        loadChildren: () => import('./content/registration/registration.module').then(m => m.RegistrationModule),
        resolve: {
          translation: TranslateResolver,
        },
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
