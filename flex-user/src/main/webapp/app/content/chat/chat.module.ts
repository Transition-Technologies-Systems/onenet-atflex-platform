import { SelectButtonModule } from 'primeng/selectbutton';

import { NgModule } from '@angular/core';
import { SharedModule } from '@app/shared';

import { ChatComponent } from './chat.component';
import { ChatRoutingModule } from './chat.routing';
import { ChatService } from './chat.service';
import { NewChatComponent } from './new-chat/new-chat.component';

@NgModule({
  imports: [SharedModule, SelectButtonModule, ChatRoutingModule],
  declarations: [ChatComponent, NewChatComponent],
  providers: [ChatService],
})
export class ChatModule {}
