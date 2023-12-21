import * as moment from 'moment';
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

import { AppCalendarLibrary } from './library/calendar';

@Component({
  selector: 'app-calendar',
  templateUrl: './calendar.component.html',
  styleUrls: ['./calendar.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,

  // tslint:disable-next-line:no-host-metadata-property
  host: {
    '[class.p-inputwrapper-filled]': 'filled',
    '[class.p-inputwrapper-focus]': 'focused || overlayVisible',
  },
})
export class CalendarComponent implements ControlValueAccessor, OnInit, OnDestroy, AfterViewInit {
  @Input() showIcon = true;
  @Input() showTime = false;
  @Input() showSeconds = false;
  @Input() timeOnly = false;
  @Input() showMinute = true;
  @Input() show24Hour = false;
  @Input() format = 'dd/mm/yy';
  @Input() styleClass = '';
  @Input() stepMinute: number = 1;
  @Input()
  set minDate(value: Date | undefined) {
    this._minDate = value;

    if (this.value && value) {
      if (moment(this.value).isBefore(moment(value))) {
        this.onChangeValue(value);
      }
    }
  }
  @Input()
  set maxDate(value: Date | undefined) {
    this._maxDate = value;

    if (this.value && value) {
      if (moment(this.value).isAfter(moment(value))) {
        this.onChangeValue(value);
      }
    }
  }
  @Input() dayForTime: Date | undefined;
  @Input() inputId = 'field-calendar';
  @Input() icon = 'icon icon--calendar';
  @Input() selectionMode: 'single' | 'multiple' | 'range' = 'single';

  @ViewChild(AppCalendarLibrary) calendar: AppCalendarLibrary | undefined;

  // tslint:disable-next-line:no-output-on-prefix
  @Output() onChange = new EventEmitter<any>();

  value: any[] = [];

  focused = false;
  filled = false;
  disabled = false;
  overlayVisible = false;

  private _maxDate: Date | undefined;
  private _minDate: Date | undefined;

  get min(): Date {
    return this._minDate as Date;
  }

  get max(): Date {
    return this._maxDate as Date;
  }

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

  ngOnDestroy(): void {
    this.stateChanges.complete();
  }

  ngAfterViewInit(): void {
    if (!this.calendar) {
      return;
    }

    this.calendar.updateUI = () => {
      if (!this.calendar) {
        return;
      }

      let val = this.calendar.value || this.calendar.defaultDate || new Date();

      if (Array.isArray(val)) {
        val = val[0] || new Date();
      }

      this.calendar.currentMonth = val.getMonth();
      this.calendar.currentYear = val.getFullYear();
      this.calendar.createMonths(this.calendar.currentMonth, this.calendar.currentYear);

      if (this.showTime || this.calendar.timeOnly) {
        if (this.minDate && moment(val).isBefore(moment(this.minDate))) {
          val = this.minDate;
        }

        let minutes = val.getMinutes();

        if (this.stepMinute !== 1 && minutes) {
          const check = minutes % this.stepMinute;

          if (check !== 0) {
            minutes = minutes + this.stepMinute - check;
          }
        }

        this.calendar.setCurrentHourPM(val.getHours());
        this.calendar.currentMinute = minutes;
        this.calendar.currentSecond = val.getSeconds();
      }
    };
  }

  onChangeValue(value: any): void {
    let newValue = this.showTime && !this.showMinute && value ? moment(value).set({ m: 0, s: 0, ms: 0 }).toDate() : value;

    this.filled = this.isFilled(value);
    this.value = newValue;

    this.ngChange(newValue);

    this.onChange.emit(newValue);
  }

  onBlur(): void {
    this.focused = false;

    if (!Array.isArray(this.value)) {
      const { correct, value } = this.correctMinute(this.value);

      if (correct) {
        this.onChangeValue(value);
      }
    }

    this.ngTouched();
  }

  onFocus(): void {
    this.focused = true;
  }

  setDisabledState(disabled: boolean): void {
    this.disabled = disabled;

    this.stateChanges.next();
  }

  writeValue(value: any): void {
    const currentValue = this.value;

    const { correct, value: correctValue } = this.correctMinute(value);

    if (correct) {
      value = correctValue;
    }

    this.filled = this.isFilled(value);
    this.value = value;

    if (currentValue !== value) {
      this.cdr.markForCheck();
    }
  }

  registerOnChange(fn: any): void {
    this.ngChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.ngTouched = fn;
  }

  private correctMinute(date?: Date): { correct: boolean; value?: Date } {
    if (this.stepMinute !== 1 && date) {
      const minute = moment(date).minute();
      const check = minute % this.stepMinute;

      if (check !== 0) {
        const value = moment(date)
          .add(this.stepMinute - check, 'm')
          .toDate();

        return { correct: true, value };
      }
    }

    return { correct: false };
  }

  private isFilled(value: any | any[]): boolean {
    if (Array.isArray(value)) {
      return !!value[0] || !!value[1];
    }

    return !!value;
  }
}
