import { AlertsStore } from './alert.store';
import { MessageTableComponent } from './message-table/message-table.component';
import { NgModule } from '@angular/core';
import { NotificationsComponent } from './notifications.component';
import { NotificationsPreviewComponent } from './preview';
import { NotificationsService } from './notifications.service';
import { NotificationsStore } from './notifications.store';
import { SharedCommonsModule } from '../commons/shared-commons.module';
import { SharedLibraryModule } from '../shared-library.module';

@NgModule({
  imports: [SharedLibraryModule, SharedCommonsModule],
  declarations: [NotificationsComponent, NotificationsPreviewComponent, MessageTableComponent],
  exports: [NotificationsComponent, NotificationsPreviewComponent],
  providers: [AlertsStore, NotificationsService, NotificationsStore],
})
export class NotificationsModule {}
