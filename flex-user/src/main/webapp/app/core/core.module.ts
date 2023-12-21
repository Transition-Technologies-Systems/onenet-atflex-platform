import { ngxLoadingAnimationTypes, NgxLoadingModule } from 'ngx-loading';
import { MessageService } from 'primeng/api';
import { ToastModule } from 'primeng/toast';

import { CommonModule, registerLocaleData } from '@angular/common';
import { HTTP_INTERCEPTORS, HttpClient, HttpClientJsonpModule, HttpClientModule } from '@angular/common/http';
import locale from '@angular/common/locales/pl';
import { Injector, LOCALE_ID, NgModule, Optional, SkipSelf } from '@angular/core';
import { BUILD } from '@env/build';
import { EffectsModule } from '@ngrx/effects';
import { RouterState, RouterStateSerializer, StoreRouterConnectingModule } from '@ngrx/router-store';
import { StoreModule } from '@ngrx/store';
import { StoreDevtoolsModule } from '@ngrx/store-devtools';
import { MissingTranslationHandler, TranslateLoader, TranslateModule, TranslateParser } from '@ngx-translate/core';
import { TranslateHttpLoader } from '@ngx-translate/http-loader';

import { AuthInterceptor } from './auth/auth.interceptor';
import { AuthService } from './auth/auth.service';
import { AuthEffects } from './auth/effects';
import { HasKeyParamGuard } from './auth/guard/has-key-param.guard';
import { IsAuthenticatedGuard } from './auth/guard/is-authenticated.guard';
import { UserRouteAccessService } from './auth/guard/user-route-access.guard';
import { metaReducers, ROOT_REDUCERS } from './core.state';
import { LanguageEffects } from './language/effects';
import { AppTranslateParser } from './language/language-parser.service';
import { AppMissingTranslationHandler, LanguageService } from './language/language.service';
import { LoadingInterceptor } from './loading/loading.interceptor';
import { LoadingService } from './loading/loading.service';
import { ServiceLocator } from './locator.service';
import { RouterSerializer } from './router/router-serializer';
import { LocalStorageService } from './storage/local-storage.service';
import { SessionStorageService } from './storage/session-storage.service';
import { TitleService } from './title/title.service';
import { AppToastrService } from './toastr/toastr.service';
import { IsNotAuthenticatedGuard } from './auth/guard/is-not-authenticated.guard';

@NgModule({
  imports: [
    CommonModule,
    HttpClientModule,
    HttpClientJsonpModule,

    ToastModule,

    StoreRouterConnectingModule.forRoot(),
    StoreModule.forRoot(ROOT_REDUCERS, {
      metaReducers,
      runtimeChecks: {
        strictStateImmutability: true,
        strictActionImmutability: true,
        strictStateSerializability: true,
        strictActionSerializability: false,
      },
    }),
    StoreRouterConnectingModule.forRoot({
      routerState: RouterState.Minimal,
    }),
    EffectsModule.forRoot([AuthEffects, LanguageEffects]),
    StoreDevtoolsModule.instrument({
      maxAge: 10,
    }),

    TranslateModule.forRoot({
      loader: {
        provide: TranslateLoader,
        useFactory: HttpLoaderFactory,
        deps: [HttpClient],
      },
      parser: { provide: TranslateParser, useClass: AppTranslateParser },
      missingTranslationHandler: {
        provide: MissingTranslationHandler,
        useClass: AppMissingTranslationHandler,
      },
      isolate: true,
    }),
    NgxLoadingModule.forRoot({
      animationType: ngxLoadingAnimationTypes.wanderingCubes,
      backdropBackgroundColour: 'rgba(0, 0, 0, 0.5)',
      backdropBorderRadius: '0px',
      primaryColour: '#ffffff',
      secondaryColour: '#ffffff',
      tertiaryColour: '#ffffff',
      fullScreenBackdrop: true,
    }),
  ],
  providers: [
    AuthService,
    TitleService,
    LoadingService,
    LanguageService,
    LocalStorageService,
    SessionStorageService,
    UserRouteAccessService,
    HasKeyParamGuard,
    IsAuthenticatedGuard,
    IsNotAuthenticatedGuard,
    AppToastrService,
    MessageService,
    { provide: LOCALE_ID, useValue: 'pl' },
    { provide: RouterStateSerializer, useClass: RouterSerializer },
    { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true },
    { provide: HTTP_INTERCEPTORS, useClass: LoadingInterceptor, multi: true },
  ],
  exports: [ToastModule, TranslateModule, NgxLoadingModule],
})
export class CoreModule {
  constructor(
    private injector: Injector,
    @Optional()
    @SkipSelf()
    parentModule: CoreModule
  ) {
    if (parentModule) {
      throw new Error('CoreModule is already loaded. Import only in AppModule');
    }

    registerLocaleData(locale);

    ServiceLocator.injector = this.injector;
  }
}

/**
 * Factory for load files to translation
 *
 * @param http This is istance HttpClient
 */
export function HttpLoaderFactory(http: HttpClient): TranslateHttpLoader {
  return new TranslateHttpLoader(http, 'assets/i18n/', `.json?v=${BUILD.timestamp}`);
}
