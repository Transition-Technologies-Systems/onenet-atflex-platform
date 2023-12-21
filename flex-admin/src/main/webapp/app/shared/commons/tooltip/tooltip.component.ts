import { Component } from '@angular/core';

@Component({
  selector: 'app-tooltip',
  template: `
    <div #contentWrapper [pTooltip]="contentWrapper.textContent ?? ''">
      <ng-content></ng-content>
    </div>
  `,
})
export class TooltipComponent {}
