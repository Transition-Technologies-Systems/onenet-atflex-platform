import { ActivatedRoute, Router } from '@angular/router';
import { Component, OnInit, ViewEncapsulation } from '@angular/core';
import { Tab, TabType } from './scheduling-units';

import { ConfirmationService } from 'primeng/api';
import { SchedulingUnitsService } from './scheduling-units.service';

@Component({
  selector: 'app-scheduling-unit',
  templateUrl: './scheduling-units.component.html',
  styleUrls: ['./scheduling-units.component.scss'],
  providers: [ConfirmationService],
  encapsulation: ViewEncapsulation.None,
})
export class SchedulingUnitsComponent implements OnInit {
  selectedTab: TabType = 'list';
  isRegister = false;

  constructor(route: ActivatedRoute, private router: Router, private service: SchedulingUnitsService) {
    this.isRegister = route.snapshot.data?.type === 'REGISTER';
  }

  ngOnInit(): void {
    this.selectedTab = this.router.url.includes('types') ? 'types-su' : 'list';
  }

  changeTab(): void {
    switch (this.selectedTab) {
      case 'list':
        this.router.navigate(['/scheduling-units']);
        break;
      case 'types-su':
        this.router.navigate(['/scheduling-units/types']);
        break;
    }
  }

  getTabs(): Tab[] {
    return this.service.getTabs();
  }
}
