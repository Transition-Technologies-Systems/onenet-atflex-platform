import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UserProfileComponent } from './user-profile.component';
import { SharedModule } from '@app/shared';
import { UserProfileService } from './user-profile.service';
import { UserProfileRoutingModule } from './user-profile.routing';
import { AccordionModule } from 'primeng/accordion';

@NgModule({
  declarations: [UserProfileComponent],
  imports: [CommonModule, SharedModule, UserProfileRoutingModule, AccordionModule],
  providers: [UserProfileService],
})
export class UserProfileModule {}
