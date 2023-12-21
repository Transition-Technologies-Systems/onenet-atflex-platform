import { Component, OnInit } from '@angular/core';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { ForecastedPricesDetailDTO, ForecastedPricesFileDTO } from '../forecasted-prices';

import { ProductsForecastedPricessService } from '../forecasted-prices.service';

@Component({
  selector: 'app-products-forecasted-prices-preview',
  templateUrl: './preview.component.html',
  styleUrls: ['./preview.component.scss'],
})
export class ProductsForecastedPricesPreviewComponent implements OnInit {
  versionId: number = this.config?.data.id;
  data: ForecastedPricesFileDTO = this.config?.data;

  details: ForecastedPricesDetailDTO[] = [];
  hours: string[] = Array.from({ length: 25 }, (_, i: number) => (i === 24 ? '2a' : `${i + 1}`));

  constructor(private service: ProductsForecastedPricessService, public config: DynamicDialogConfig, public ref: DynamicDialogRef) {}

  ngOnInit(): void {
    this.getData();
  }

  changeVersion(): void {
    this.getData();
  }

  close(): void {
    this.ref.close();
  }

  private getData(): void {
    this.service.getForecastedPricesDetail(this.data.id).subscribe(response => {
      if (response) {
        this.hours = response.prices.map(({ id }) => id);
      }

      this.details = response ? [response] : [];
    });
  }
}
