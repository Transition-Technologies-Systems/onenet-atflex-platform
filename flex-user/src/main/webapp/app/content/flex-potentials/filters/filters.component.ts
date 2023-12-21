import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { Observable, Subject, of } from 'rxjs';

import { ActivatedRoute } from '@angular/router';
import { Dictionary } from '@app/shared/models';
import { FlexPotentialsFiltersService } from './filters.service';
import { FlexPotentialsService } from '../flex-potentials.service';
import { UntypedFormGroup } from '@angular/forms';

interface Dictionaries {
  ders$: Observable<Dictionary[]>;
}

@Component({
  selector: 'app-flex-potentials-filters',
  templateUrl: './filters.component.html',
  styleUrls: ['./filters.component.scss'],
})
export class FlexPotentialsFiltersComponent implements OnInit, OnDestroy {
  @Input() initFilters: any;

  dictionaries: Dictionaries = {
    ders$: of([]),
  };

  filters: UntypedFormGroup = this.filtersService.createFormFilter();

  @Output() filterData = new EventEmitter<object>();

  private isRegister = false;
  private destroyed$ = new Subject<void>();

  constructor(
    route: ActivatedRoute,
    private filtersService: FlexPotentialsFiltersService,
    private flexPotentialsService: FlexPotentialsService
  ) {
    this.isRegister = route.snapshot.data?.type === 'REGISTER';

    this.dictionaries.ders$ = this.flexPotentialsService.getDerNames(this.isRegister);
  }

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
