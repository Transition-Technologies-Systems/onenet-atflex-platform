import { Component, ViewEncapsulation } from '@angular/core';

@Component({
  selector: 'app-unlogged-main-page',
  template: `
    <div id="wrapper-unlogged-page">
      <router-outlet></router-outlet>
    </div>
  `,
  styleUrls: ['./main.component.scss'],
  encapsulation: ViewEncapsulation.None,
})
export class UnloggedMainPageComponent {
  constructor() {}
}
