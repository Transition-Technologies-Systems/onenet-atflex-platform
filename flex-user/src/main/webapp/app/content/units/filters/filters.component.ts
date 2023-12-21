import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { DerTypeDTO, TabType } from '../unit';
import { Observable, Subject, of } from 'rxjs';

import { BooleanEnum } from '@app/shared/enums';
import { Dictionary } from '@app/shared/models';
import { UntypedFormGroup } from '@angular/forms';
import { Helpers } from '@app/shared/commons';
import { UnitsFiltersService } from './filters.service';
import { UnitsService } from '../units.service';

interface Dictionaries {
  boolean: Observable<Dictionary[]>;
  units: Observable<Dictionary[]>;
}

@Component({
  selector: 'app-units-filters',
  templateUrl: './filters.component.html',
  styleUrls: ['./filters.component.scss'],
})
export class UnitsFiltersComponent implements OnInit, OnDestroy {
  @Input() initFilters: any;
  @Input() type: TabType = 'list';

  unitTypes!: DerTypeDTO[];

  dictionaries$: Dictionaries = {
    boolean: of(Helpers.enumToDictionary<boolean>(BooleanEnum, 'Boolean')),
    units: this.service.getUnits(),
  };
  filters: UntypedFormGroup = this.filtersService.createFormFilter();

  @Output() filterData = new EventEmitter<object>();

  private destroyed$ = new Subject<void>();

  constructor(private service: UnitsService, private filtersService: UnitsFiltersService) {}

  filter(): void {
    this.filterData.emit(this.filters.getRawValue());
  }

  ngOnInit(): void {
    this.service.getDerTypesDict().subscribe((response: DerTypeDTO[]) => {
      this.unitTypes = response;
    });
    if (this.initFilters) {
      this.filters.patchValue(this.initFilters);
    }
  }

  ngOnDestroy(): void {
    this.destroyed$.next();
    this.destroyed$.complete();
  }
}
