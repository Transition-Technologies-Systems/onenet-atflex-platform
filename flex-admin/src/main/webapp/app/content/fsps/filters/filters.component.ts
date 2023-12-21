import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';

import { BooleanEnum } from '@app/shared/enums';
import { Dictionary } from '@app/shared/models';
import { UntypedFormGroup } from '@angular/forms';
import { FspsFiltersService } from './filters.service';
import { Helpers } from '@app/shared/commons';
import { Subject } from 'rxjs';

interface Dictionaries {
  boolean: Dictionary[];
}

@Component({
  selector: 'app-fsps-filters',
  templateUrl: './filters.component.html',
  styleUrls: ['./filters.component.scss'],
})
export class FspsFiltersComponent implements OnInit, OnDestroy {
  @Input() initFilters: any;

  dictionaries: Dictionaries = {
    boolean: Helpers.enumToDictionary<boolean>(BooleanEnum, 'Boolean'),
  };
  filters: UntypedFormGroup = this.filtersService.createFormFilter();

  @Output() filterData = new EventEmitter<object>();

  private destroyed$ = new Subject<void>();

  constructor(private filtersService: FspsFiltersService) {}

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
