import { Subject } from 'rxjs';

import {
  AfterViewInit,
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  EventEmitter,
  Input,
  OnDestroy,
  OnInit,
  Optional,
  Output,
  Self,
  ViewChild,
} from '@angular/core';
import { ControlValueAccessor, NgControl } from '@angular/forms';
import { Column } from '@app/shared/models';

import { MultiselectComponent } from '../multiselect/multiselect.component';

@Component({
  selector: 'app-columns-configuration',
  templateUrl: './columns-configuration.component.html',
  styleUrls: ['./columns-configuration.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ColumnsConfigurationComponent implements ControlValueAccessor, OnInit, OnDestroy, AfterViewInit {
  @Input() columns: Column[] = [];

  @ViewChild(MultiselectComponent) multiselect: MultiselectComponent | undefined;

  // tslint:disable-next-line:no-output-on-prefix
  @Output() onChange = new EventEmitter<any>();

  value: any[] = [];

  private stateChanges = new Subject<void>();
  private ngChange = (value: any) => {};
  private ngTouched = () => {};

  constructor(private cdr: ChangeDetectorRef, @Optional() @Self() public ngControl: NgControl) {
    if (this.ngControl != null) {
      this.ngControl.valueAccessor = this;
    }
  }

  ngOnInit(): void {
    this.stateChanges.subscribe(() => this.cdr.markForCheck());
  }

  ngAfterViewInit(): void {
    const multiselect = this.multiselect?.multiselect;

    if (!this.multiselect) {
      return;
    }

    if (!this.multiselect?.multiselect) {
      return;
    }

    this.multiselect.multiselect.isOutsideClicked = event => {
      const className = (event.target as any).className || '';

      if (className.includes('icon--hidde-columns') || className.includes('columns-configuration')) {
        return false;
      }

      return !(
        multiselect?.el.nativeElement.isSameNode(event.target) ||
        multiselect?.el.nativeElement.contains(event.target) ||
        multiselect?.isOverlayClick(event)
      );
    };
  }

  ngOnDestroy(): void {
    this.stateChanges.complete();
  }

  onChangeValue(value: any[]): void {
    this.ngChange(value);

    this.onChange.emit(value);
  }

  open(): void {
    this.multiselect?.show();
  }

  writeValue(value: any): void {
    this.value = value;
  }

  registerOnChange(fn: any): void {
    this.ngChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.ngTouched = fn;
  }
}
