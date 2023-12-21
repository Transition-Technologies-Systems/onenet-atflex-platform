import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { Observable, Subject } from 'rxjs';

import { BooleanEnum } from '@app/shared/enums';
import { Dictionary } from '@app/shared/models';
import { UntypedFormGroup } from '@angular/forms';
import { Helpers } from '@app/shared/commons';
import { ProductsFiltersService } from './filters.service';
import { ProductsService } from '../products.service';
import { TabType } from '../product';

interface Dictionaries {
  boolean: Dictionary[];
  products: Observable<Dictionary[]>;
}

@Component({
  selector: 'app-products-filters',
  templateUrl: './filters.component.html',
  styleUrls: ['./filters.component.scss'],
})
export class ProductsFiltersComponent implements OnInit, OnDestroy {
  @Input() initFilters: any;
  @Input() type: TabType = 'list';

  dictionaries: Dictionaries = {
    boolean: Helpers.enumToDictionary<boolean>(BooleanEnum, 'Boolean'),
    products: this.service.getProducts(),
  };
  filters: UntypedFormGroup = this.filtersService.createFormFilter();

  @Output() filterData = new EventEmitter<object>();

  private destroyed$ = new Subject<void>();

  constructor(private service: ProductsService, private filtersService: ProductsFiltersService) {}

  filter(): void {
    this.filterData.emit(this.filters.getRawValue());
  }

  ngOnInit(): void {
    if (this.initFilters) {
      this.filters.patchValue(this.initFilters);
    }
  }

  ngOnDestroy(): void {
    this.destroyed$.next();
    this.destroyed$.complete();
  }
}
