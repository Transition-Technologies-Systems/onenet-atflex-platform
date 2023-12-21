import { NgModule } from '@angular/core';
import { SharedModule } from '@app/shared';

import { AccountRoutingModule } from './account.routing';
import { AccountService } from './account.service';
import { ActivateAccountComponent } from './activate';
import { ResetPasswordComponent } from './reset-password';

@NgModule({
  imports: [SharedModule, AccountRoutingModule],
  declarations: [ActivateAccountComponent, ResetPasswordComponent],
  providers: [AccountService],
})
export class AccountModule {}
