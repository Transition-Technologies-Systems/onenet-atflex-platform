import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { Dictionary, ProductDTO } from '@app/shared/models';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';

import { ProductsService } from '../products.service';

@Component({
  selector: 'app-products-preview',
  styles: [
    `
      .name {
        margin: 1rem 0;
      }

      .name:first-of-type {
        margin-top: 0;
      }
    `,
  ],
  templateUrl: './preview.component.html',
})
export class ProductsPreviewComponent implements OnInit {
  versionId: number = this.config?.data.id;
  data: ProductDTO = this.config?.data;

  users: Dictionary[] = [];

  constructor(
    public ref: DynamicDialogRef,
    private service: ProductsService,
    private cdr: ChangeDetectorRef,
    public config: DynamicDialogConfig
  ) {}

  ngOnInit(): void {
    this.getData();
  }

  changeVersion(): void {
    this.getData();
  }

  close(): void {
    this.ref.close();
  }

  onDownloadFile(id: number): void {
    this.service.downloadFile(id);
  }

  private getData(): void {
    this.service.getProduct(this.versionId).subscribe((response: ProductDTO) => {
      this.data = response;
      this.cdr.markForCheck();
    });
  }
}
