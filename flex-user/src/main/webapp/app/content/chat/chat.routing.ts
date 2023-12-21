import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { TranslateResolver } from '@app/core';

import { ChatComponent } from './chat.component';

const routes: Routes = [
  {
    path: '',
    component: ChatComponent,
    data: {
      i18n: ['chat'],
      pageTitle: 'global.title',
    },
    resolve: {
      translation: TranslateResolver,
    },
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class ChatRoutingModule {}
