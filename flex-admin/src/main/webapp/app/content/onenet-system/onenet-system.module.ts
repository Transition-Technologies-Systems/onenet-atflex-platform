import { NgModule } from '@angular/core';
import { OneNetSystemRoutingModule } from './onenet-system.routing';
import { SharedModule } from '@app/shared';
import { OneNetSystemComponent } from './onenet-system.component';
import { OnsUsersComponent } from './ons-users/ons-users.component';
import { OnsUsersService } from './ons-users/ons-users.service';
import { OnsUsersDialogComponent } from './ons-users/ons-users-dialog/ons-users-dialog.component';
import { ConsumeDataComponent } from './consume-data/consume-data.component';
import { ConsumeDataService } from './consume-data/consume-data.service';
import { DescriptionPreviewModalComponent } from './description-preview-modal/description-preview-modal.component';
import { OfferedServicesComponent } from './offered-services/offered-services.component';
import { OfferedServicesService } from './offered-services/offered-services.service';
import { ProvideDialogComponent } from './provide-dialog/provide-dialog.component';
import { ProvideDataComponent } from './provide-data/provide-data.component';
import { ProvideDataService } from './provide-data/provide-data.service';
import { ProvideDialogService } from './provide-dialog/provide-dialog.service';

@NgModule({
  imports: [OneNetSystemRoutingModule, SharedModule],
  declarations: [
    OneNetSystemComponent,
    OnsUsersComponent,
    OnsUsersDialogComponent,
    ConsumeDataComponent,
    DescriptionPreviewModalComponent,
    OfferedServicesComponent,
    ProvideDialogComponent,
    ProvideDataComponent,
  ],
  providers: [OnsUsersService, ConsumeDataService, OfferedServicesService, ProvideDataService, ProvideDialogService],
})
export class OneNetSystemModule {}
