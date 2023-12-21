import { NgModule } from '@angular/core';
import { SharedModule } from '@app/shared';

import { UsersDialogComponent } from './dialog';
import { UsersComponent } from './users.component';
import { UsersRoutingModule } from './users.routing';
import { UsersService } from './users.service';
import { UsersStore } from './users.store';

@NgModule({
  imports: [SharedModule, UsersRoutingModule],
  declarations: [UsersComponent, UsersDialogComponent],
  providers: [UsersService, UsersStore],
})
export class UsersModule {}
