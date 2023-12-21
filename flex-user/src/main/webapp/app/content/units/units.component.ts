import { Component, OnInit, ViewEncapsulation } from '@angular/core';
import { Tab, TabType } from './unit';

import { ConfirmationService } from 'primeng/api';
import { Router } from '@angular/router';
import { UnitsService } from './units.service';
import { ViewConfigurationService } from '@app/shared/commons/view-configuration';

@Component({
  selector: 'app-units',
  templateUrl: './units.component.html',
  styleUrls: ['./units.component.scss'],
  providers: [ConfirmationService],
  encapsulation: ViewEncapsulation.None,
})
export class UnitsComponent implements OnInit {
  selectedTab: TabType = 'list';

  constructor(private router: Router, private service: UnitsService, protected viewConfigurationService: ViewConfigurationService) {}

  ngOnInit(): void {
    this.selectedTab = this.router.url.includes('self-schedules') ? 'self-schedules' : 'list';
  }

  changeTab(): void {
    switch (this.selectedTab) {
      case 'list':
        this.router.navigate(['/ders/list']);
        break;
      case 'self-schedules':
        this.router.navigate(['/ders/self-schedules']);
        break;
    }
  }

  getTabs(): Tab[] {
    return this.service.getTabs();
  }
}
