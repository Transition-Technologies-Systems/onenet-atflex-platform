import { ChangeDetectionStrategy, ChangeDetectorRef, Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { Dictionary, ProductDTO } from '@app/shared/models';

import { ProductsService } from '../../products.service';

@Component({
  selector: 'app-product-preview',
  templateUrl: './product-preview.component.html',
  styleUrls: ['./product-preview.component.scss'],
  providers: [ProductsService],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProductPreviewComponent implements OnInit, OnChanges {
  @Input() productData: ProductDTO | undefined;
  @Input() productId = -1;
  @Input() embeddedPreview = false;
  @Input() selected = false;
  @Input() borderLeft = true;

  data: ProductDTO | undefined = undefined;
  users: Dictionary[] = [];

  constructor(private service: ProductsService, public cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.getUsers();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes?.productData?.currentValue) {
      this.data = this.productData;
    }

    if (changes?.productId?.currentValue) {
      this.service.getProduct(this.productId).subscribe((product: ProductDTO) => {
        this.data = product;
        this.cdr.markForCheck();
      });
    }
  }

  private getUsers(): void {
    this.service.getUsers().subscribe((response: Dictionary[]) => {
      this.users = response;
      this.cdr.markForCheck();
    });
  }
}
