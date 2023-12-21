import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { Observable, Subject, of } from 'rxjs';

import { BooleanEnum } from '@app/shared/enums';
import { Dictionary } from '@app/shared/models';
import { UntypedFormGroup } from '@angular/forms';
import { Helpers } from '@app/shared/commons';
import { SchedulingUnitsFiltersService } from './filters.service';
import { SchedulingUnitsService } from '../scheduling-units.service';

interface Dictionaries {
  schedulingUnitTypes: Observable<Dictionary[]>;
  boolean: Observable<Dictionary[]>;
}

@Component({
  selector: 'app-scheduling-unit-filters',
  templateUrl: './filters.component.html',
  styleUrls: ['./filters.component.scss'],
})
export class SchedulingUnitsFiltersComponent implements OnInit, OnDestroy {
  @Input() initFilters: any;
  @Input() isRegister = false;

  dictionaries$: Dictionaries = {
    schedulingUnitTypes: this.service.getSchedulungUnitTypes(),
    boolean: of(Helpers.enumToDictionary<boolean>(BooleanEnum, 'Boolean')),
  };
  filters: UntypedFormGroup = this.filtersService.createFormFilter();

  @Output() filterData = new EventEmitter<object>();

  private destroyed$ = new Subject<void>();

  constructor(private service: SchedulingUnitsService, private filtersService: SchedulingUnitsFiltersService) {}

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
