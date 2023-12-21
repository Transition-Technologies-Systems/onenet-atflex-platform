import { NgModule } from '@angular/core';
import { SharedModule } from '@app/shared';

import { RegistrationConfirmComponent } from './confirm';
import { RegistrationComponent } from './registration.component';
import { RegistrationRoutingModule } from './registration.routing';
import { RegistrationService } from './registration.service';
import { RegistrationRejectComponent } from './reject';

@NgModule({
  imports: [SharedModule, RegistrationRoutingModule],
  declarations: [RegistrationComponent, RegistrationConfirmComponent, RegistrationRejectComponent],
  providers: [RegistrationService],
})
export class RegistrationModule {}
