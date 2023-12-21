import { NgModule } from '@angular/core';
import { SharedModule } from '@app/shared';

import { ForgotPasswordComponent } from './forgot-password';
import { LoginComponent } from './login.component';
import { LoginRoutingModule } from './login.routing';
import { LoginService } from './login.service';

@NgModule({
  imports: [SharedModule, LoginRoutingModule],
  declarations: [LoginComponent, ForgotPasswordComponent],
  providers: [LoginService],
})
export class LoginModule {}
