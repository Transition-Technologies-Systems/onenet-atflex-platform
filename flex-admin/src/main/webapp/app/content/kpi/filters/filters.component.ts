import { takeUntil } from 'rxjs/operators';
import { Subject, tap, BehaviorSubject} from 'rxjs';
import { Component, OnInit, Output, EventEmitter, OnDestroy, ChangeDetectorRef, AfterViewInit } from '@angular/core';
import { UntypedFormGroup } from '@angular/forms';
import { KpiFiltersService } from './filters.service';
import { Dictionary } from '@app/shared/models';
import { KpiService } from '../kpi.service';

interface Dictionaries {
  type: Dictionary[];
}

@Component({
  selector: 'app-kpi-filters',
  templateUrl: './filters.component.html',
  styleUrls: ['./filters.component.scss'],
})
export class KpiFiltersComponent implements OnInit, OnDestroy, AfterViewInit {
  dictionaries: Dictionaries = {
    type: [],
  };
  filters: UntypedFormGroup = this.service.createFormFilter();
  $showDateFilters = new BehaviorSubject<boolean>(false);
  init = true;

  @Output() filterData = new EventEmitter<object>();

  private destroyed$ = new Subject<void>();
  constructor(private service: KpiFiltersService, private cdr: ChangeDetectorRef, private kpiService: KpiService) {}

  filter(): void {
    this.filterData.emit(this.filters.getRawValue());
  }

  ngOnInit(): void {
    this.subscribeKpiFilter();
    this.kpiService
      .getKpiTypes()
      .pipe(
        tap(kpiTypes => {
          this.dictionaries.type = kpiTypes;
        })
      )
      .subscribe();
  }

  ngAfterViewInit(): void {
    this.cdr.detectChanges();
  }

  ngOnDestroy(): void {
    this.destroyed$.next();
    this.destroyed$.complete();
  }

  private subscribeKpiFilter(): void {
    const { type, date } = this.filters.controls;
    type.valueChanges
      .pipe(
        takeUntil(this.destroyed$),
        tap(value => {
          this.init = false;
          if (value) {
            const showDateFilters = this.dictionaries.type.filter(item => item.value === value)[0].filterDate;
            if (showDateFilters) {
              date.enable();
            } else {
              date.setValue({ from: null, to: null });
              date.disable();
            }
            this.$showDateFilters.next(showDateFilters);
          } else {
            this.$showDateFilters.next(false);
            date.setValue({ from: null, to: null });
            date.disable();
          }
          this.cdr.detectChanges();
          this.init = true;
        })
      )
      .subscribe();
  }
}
