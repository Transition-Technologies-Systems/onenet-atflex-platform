import {
  ChangeDetectionStrategy,
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnDestroy,
  OnInit,
  Output,
  SimpleChanges,
} from '@angular/core';
import { Observable, Subject } from 'rxjs';

import { AuctionsDayAheadFiltersService } from './filters.service';
import { DayAheadService } from '../day-ahead.service';
import { Dictionary } from '@app/shared/models';
import { TabType } from '../day-ahead';
import { UntypedFormGroup } from '@angular/forms';

interface Dictionaries {
  products$: Observable<Dictionary[]>;
}

@Component({
  selector: 'app-auctions-day-ahead-filters',
  templateUrl: './filters.component.html',
  styleUrls: ['./filters.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AuctionsDayAheadFiltersComponent implements OnInit, OnDestroy, OnChanges {
  @Input() tab: TabType = 'energy-auctions';
  @Input() initFilters: any;

  dictionaries: Dictionaries = {
    products$: this.service.getAuctionProducts(this.service.tabTypeToType(this.tab)),
  };
  filters: UntypedFormGroup = this.filtersService.createFormFilter();

  @Output() filterData = new EventEmitter<object>();

  private destroyed$ = new Subject<void>();

  constructor(private filtersService: AuctionsDayAheadFiltersService, private service: DayAheadService) {}

  filter(): void {
    this.filterData.emit(this.filters.getRawValue());
  }

  ngOnInit(): void {
    if (this.initFilters) {
      this.filters.patchValue(this.initFilters);
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.tab) {
      const defaultData = this.filtersService.createFormFilter().getRawValue();

      this.filters.patchValue({
        ...defaultData,
        ...this.initFilters,
      });

      this.dictionaries.products$ = this.service.getAuctionProducts(this.service.tabTypeToType(this.tab));
    }
  }

  ngOnDestroy(): void {
    this.destroyed$.next();
    this.destroyed$.complete();
  }
}
