import { NgModule } from '@angular/core';
import { SharedModule } from '@app/shared';

import { RegistrationThreadComponent } from './registration-thread.component';
import { RegistrationThreadResolver } from './registration-thread.resolver';
import { RegistrationThreadRoutingModule } from './registration-thread.routing';
import { RegistrationThreadService } from './registration-thread.service';

@NgModule({
  imports: [SharedModule, RegistrationThreadRoutingModule],
  declarations: [RegistrationThreadComponent],
  providers: [RegistrationThreadService, RegistrationThreadResolver],
})
export class RegistrationThreadModule {}
