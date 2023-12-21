import { Subject } from 'rxjs';

import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { UntypedFormGroup } from '@angular/forms';
import { Helpers } from '@app/shared/commons';
import { BooleanEnum } from '@app/shared/enums';
import { Dictionary } from '@app/shared/models';

import { ProductsFiltersService } from './filters.service';

interface Dictionaries {
  boolean: Dictionary[];
}

@Component({
  selector: 'app-products-filters',
  templateUrl: './filters.component.html',
  styleUrls: ['./filters.component.scss'],
})
export class ProductsFiltersComponent implements OnInit, OnDestroy {
  @Input() initFilters: any;

  dictionaries: Dictionaries = {
    boolean: Helpers.enumToDictionary<boolean>(BooleanEnum, 'Boolean'),
  };
  filters: UntypedFormGroup = this.filtersService.createFormFilter();

  @Output() filterData = new EventEmitter<object>();

  private destroyed$ = new Subject<void>();

  constructor(private filtersService: ProductsFiltersService) {}

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
