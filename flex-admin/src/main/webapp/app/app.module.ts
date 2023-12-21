import { APP_INITIALIZER, NgModule } from '@angular/core';
import { AuthService, CoreModule } from './core';
import { InjectableRxStompConfig, RxStompService, rxStompServiceFactory } from '@stomp/ng2-stompjs';

import { AppComponent } from './app.component';
import { AppInitializationService } from './app-initialization.service';
import { AppRoutingModule } from './app-routing.module';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { BrowserModule } from '@angular/platform-browser';
import { HTTP_INTERCEPTORS } from '@angular/common/http';
import { RxStompConfig } from './app-rx-stopm.config';
import { SharedModule } from './shared';
import { ValidatorInterceptor } from './shared/validator/validator.interceptor';
import { ValidatorService } from './shared/validator/validator.service';

export function initializeAppilcation(initializationService: AppInitializationService): () => void {
  return () => initializationService.auth();
}

@NgModule({
  declarations: [AppComponent],
  imports: [AppRoutingModule, BrowserModule, BrowserAnimationsModule, CoreModule, SharedModule],
  providers: [
    AppInitializationService,
    ValidatorService,

    { provide: InjectableRxStompConfig, useClass: RxStompConfig, deps: [AuthService] },
    {
      provide: RxStompService,
      useFactory: rxStompServiceFactory,
      deps: [InjectableRxStompConfig],
    },

    { provide: HTTP_INTERCEPTORS, useClass: ValidatorInterceptor, multi: true },
    { provide: APP_INITIALIZER, useFactory: initializeAppilcation, deps: [AppInitializationService], multi: true },
  ],
  bootstrap: [AppComponent],
})
export class AppModule {}
