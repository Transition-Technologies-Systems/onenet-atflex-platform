import { NgModule } from '@angular/core';
import { SharedModule } from '@app/shared';

import { LoggedMainPageComponent } from './main/logged/main.component';
import { UnloggedMainPageComponent } from './main/unlogged/main.component';

@NgModule({
  declarations: [LoggedMainPageComponent, UnloggedMainPageComponent],
  imports: [SharedModule],
  providers: [],
})
export class ContentModule {}
