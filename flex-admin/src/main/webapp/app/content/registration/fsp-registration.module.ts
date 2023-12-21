import { SelectButtonModule } from 'primeng/selectbutton';

import { NgModule } from '@angular/core';
import { SharedModule } from '@app/shared';

import { FspRegistrationComponent } from './fsp-registration.component';
import { FspRegistrationRoutingModule } from './fsp-registration.routing';
import { FspRegistrationService } from './fsp-registration.service';
import { FspRegistrationStore } from './fsp-registration.store';
import { FspRegistrationPreviewComponent } from './preview';

@NgModule({
  imports: [SharedModule, SelectButtonModule, FspRegistrationRoutingModule],
  declarations: [FspRegistrationComponent, FspRegistrationPreviewComponent],
  providers: [FspRegistrationService, FspRegistrationStore],
})
export class FspRegistrationModule {}
