import { ChangeDetectionStrategy, Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { Observable, Subject } from 'rxjs';

import { BidsEvaluationFiltersService } from './filters.service';
import { BidsEvaluationService } from '../bids-evaluation.service';
import { Dictionary } from '@app/shared/models';
import { UntypedFormGroup } from '@angular/forms';

interface Dictionaries {
  products$: Observable<Dictionary[]>;
}

@Component({
  selector: 'app-bids-evaluation-filters',
  templateUrl: './filters.component.html',
  styleUrls: ['./filters.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [BidsEvaluationFiltersService],
})
export class BidsEvaluationFiltersComponent implements OnInit, OnDestroy {
  @Input() initFilters: any;

  dictionaries: Dictionaries = {
    products$: this.service.getProducts(),
  };
  filters: UntypedFormGroup = this.filtersService.createFormFilter();

  @Output() filterData = new EventEmitter<object>();

  private destroyed$ = new Subject<void>();

  constructor(private filtersService: BidsEvaluationFiltersService, private service: BidsEvaluationService) {}

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
