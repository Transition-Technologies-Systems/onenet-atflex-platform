import { NgModule } from '@angular/core';
import { SharedModule } from '@app/shared';

import { AccountRoutingModule } from './account.routing';
import { AccountService } from './account.service';
import { ResetPasswordComponent } from './reset-password';

@NgModule({
  imports: [SharedModule, AccountRoutingModule],
  declarations: [ResetPasswordComponent],
  providers: [AccountService],
})
export class AccountModule {}
