import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormGroup } from '@angular/forms';

@Component({
  template: '',
})
// tslint:disable-next-line:component-class-suffix
export abstract class FiltersExtends implements OnInit {
  @Input() initFilters: any;

  @Output() filterData = new EventEmitter<object>();

  filters: FormGroup;

  constructor() {}

  ngOnInit(): void {
    if (this.initFilters) {
      this.filters.patchValue(this.initFilters);
    }
  }
}
