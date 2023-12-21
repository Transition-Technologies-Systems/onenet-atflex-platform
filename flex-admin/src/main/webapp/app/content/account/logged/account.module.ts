import { NgModule } from '@angular/core';
import { SharedModule } from '@app/shared';

import { AccountRoutingModule } from './account.routing';
import { AccountService } from './account.service';
import { ChangePasswordComponent } from './change-password';

@NgModule({
  imports: [SharedModule, AccountRoutingModule],
  declarations: [ChangePasswordComponent],
  providers: [AccountService],
})
export class AccountModule {}
