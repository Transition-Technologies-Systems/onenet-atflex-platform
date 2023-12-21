import { Component, ViewEncapsulation } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ConfirmationService } from 'primeng/api';

@Component({
  selector: 'app-onenet-system',
  templateUrl: './onenet-system.component.html',
  encapsulation: ViewEncapsulation.None,
  providers: [ConfirmationService],
})
export class OneNetSystemComponent {
  seperateFilterDates = [];
  setRangeFilterDateTime = ['createdDate', 'lastModifiedDate'];

  constructor(route: ActivatedRoute) {
    route.url.subscribe(() => {
      const data = route.snapshot.firstChild?.data || {};
    });
  }
}
