import { Component, OnInit, ViewEncapsulation } from '@angular/core';
import { Tab, TabType } from './product';

import { ConfirmationService } from 'primeng/api';
import { ProductsService } from './products.service';
import { Router } from '@angular/router';
import { ViewConfigurationService } from '@app/shared/commons/view-configuration';

@Component({
  selector: 'app-products',
  templateUrl: './products.component.html',
  providers: [ConfirmationService],
  encapsulation: ViewEncapsulation.None,
})
export class ProductsComponent implements OnInit {
  selectedTab: TabType = 'list';

  constructor(private router: Router, private service: ProductsService, protected viewConfigurationService: ViewConfigurationService) {}

  ngOnInit(): void {
    this.selectedTab = this.router.url.includes('forecasted-prices') ? 'forecasted-prices' : 'list';
  }

  changeTab(): void {
    switch (this.selectedTab) {
      case 'list':
        this.router.navigate(['/products/list']);
        break;
      case 'forecasted-prices':
        this.router.navigate(['/products/forecasted-prices']);
        break;
    }
  }

  getTabs(): Tab[] {
    return this.service.getTabs();
  }
}
