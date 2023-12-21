import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';

import { AuctionsCmVcFiltersService } from './filters.service';
import { UntypedFormGroup } from '@angular/forms';
import { Subject } from 'rxjs';

@Component({
  selector: 'app-auctions-cmvc-filters',
  templateUrl: './filters.component.html',
  styleUrls: ['./filters.component.scss'],
})
export class AuctionsCmVcFiltersComponent implements OnInit, OnDestroy {
  @Input() initFilters: any;

  filters: UntypedFormGroup = this.filtersService.createFormFilter();

  @Output() filterData = new EventEmitter<object>();

  private destroyed$ = new Subject<void>();

  constructor(private filtersService: AuctionsCmVcFiltersService) {}

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
