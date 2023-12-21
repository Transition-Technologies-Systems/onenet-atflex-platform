import { ChangeDetectionStrategy, Component, Input, OnDestroy, OnInit, ViewEncapsulation, forwardRef } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { animate, state, style, transition, trigger } from '@angular/animations';

import { Calendar } from 'primeng/calendar';
import { moment } from 'polyfills';

export const CALENDAR_LIBRARY_VALUE_ACCESSOR: any = {
  provide: NG_VALUE_ACCESSOR,
  useExisting: forwardRef(() => AppCalendarLibrary),
  multi: true,
};

@Component({
  selector: 'app-library-calendar',
  templateUrl: './calendar.html',
  animations: [
    trigger('overlayAnimation', [
      state(
        'visibleTouchUI',
        style({
          transform: 'translate(-50%,-50%)',
          opacity: 1,
        })
      ),
      transition('void => visible', [
        style({ opacity: 0, transform: 'scaleY(0.8)' }),
        animate('{{showTransitionParams}}', style({ opacity: 1, transform: '*' })),
      ]),
      transition('visible => void', [animate('{{hideTransitionParams}}', style({ opacity: 0 }))]),
      transition('void => visibleTouchUI', [
        style({ opacity: 0, transform: 'translate3d(-50%, -40%, 0) scale(0.9)' }),
        animate('{{showTransitionParams}}'),
      ]),
      transition('visibleTouchUI => void', [
        animate(
          '{{hideTransitionParams}}',
          style({
            opacity: 0,
            transform: 'translate3d(-50%, -40%, 0) scale(0.9)',
          })
        ),
      ]),
    ]),
  ],
  // tslint:disable-next-line:no-host-metadata-property
  host: {
    '[class.p-inputwrapper-filled]': 'filled',
    '[class.p-inputwrapper-focus]': 'focus',
  },
  providers: [CALENDAR_LIBRARY_VALUE_ACCESSOR],
  changeDetection: ChangeDetectionStrategy.OnPush,
  encapsulation: ViewEncapsulation.None,
  styleUrls: ['./calendar.scss'],
})
// tslint:disable-next-line:component-class-suffix
export class AppCalendarLibrary extends Calendar implements OnInit, OnDestroy, ControlValueAccessor {
  @Input() showMinute = true;
  @Input() show24Hour = false;
  @Input() dayForTime: Date | undefined;

  decrementHour(event: any) {
    let newHour = this.currentHour - this.stepHour;
    let newPM = this.pm;

    if (this.hourFormat == '24') {
      if (this.show24Hour) {
        newHour = newHour < 0 ? 25 + newHour : newHour;
      } else {
        newHour = newHour < 0 ? 24 + newHour : newHour;
      }
    } else if (this.hourFormat == '12') {
      // If we were at noon/midnight, then switch
      if (this.currentHour === 12) {
        newPM = !this.pm;
      }
      newHour = newHour <= 0 ? 12 + newHour : newHour;
    }

    if (this.validateTime(newHour, this.currentMinute, this.currentSecond, newPM)) {
      this.currentHour = newHour;
      this.pm = newPM;
    }

    event.preventDefault();
  }

  decrementMinute(event: any) {
    if (this.currentHour === 24) {
      this.currentHour = 23;
    }

    let newMinute = this.currentMinute - this.stepMinute;
    newMinute = newMinute < 0 ? 60 + newMinute : newMinute;
    if (this.validateTime(this.currentHour, newMinute, this.currentSecond, this.pm)) {
      this.currentMinute = newMinute;
    }

    event.preventDefault();
  }

  formatTime(date: Date): string {
    if (!date) {
      return '';
    }

    let output = '';
    let hours = date.getHours();
    const minutes = date.getMinutes();
    const seconds = date.getSeconds();

    if (this.show24Hour && this.dayForTime) {
      if (moment(this.value).isAfter(moment(this.dayForTime).endOf('day'))) {
        hours = 24;
      }
    }

    if (this.hourFormat === '12' && hours > 11 && hours !== 12) {
      hours -= 12;
    }

    if (this.hourFormat === '12') {
      output += hours === 0 ? 12 : hours < 10 ? '0' + hours : hours;
    } else {
      output += hours < 10 ? '0' + hours : hours;
    }
    output += ':';

    if (this.showMinute) {
      output += minutes < 10 ? '0' + minutes : minutes;
    } else {
      output += '00';
    }

    if (this.showSeconds) {
      output += ':';
      output += seconds < 10 ? '0' + seconds : seconds;
    }

    if (this.hourFormat === '12') {
      output += date.getHours() > 11 ? ' PM' : ' AM';
    }

    return output;
  }

  incrementHour(event: any) {
    const prevHour = this.currentHour;
    let newHour = this.currentHour + this.stepHour;
    let newPM = this.pm;

    if (this.hourFormat == '24') {
      if (this.show24Hour) {
        newHour = newHour >= 25 ? newHour - 25 : newHour;
      } else {
        newHour = newHour >= 24 ? newHour - 24 : newHour;
      }
    } else if (this.hourFormat == '12') {
      // Before the AM/PM break, now after
      if (prevHour < 12 && newHour > 11) {
        newPM = !this.pm;
      }
      newHour = newHour >= 13 ? newHour - 12 : newHour;
    }

    if (newHour === 24) {
      this.currentMinute = 0;
    }

    if (this.validateTime(newHour, this.currentMinute, this.currentSecond, newPM)) {
      this.currentHour = newHour;
      this.pm = newPM;
    }
    event.preventDefault();
  }

  incrementMinute(event: any) {
    if (this.currentHour === 24) {
      return;
    }

    let newMinute = this.currentMinute + this.stepMinute;
    newMinute = newMinute > 59 ? newMinute - 60 : newMinute;

    if (this.validateTime(this.currentHour, newMinute, this.currentSecond, this.pm)) {
      this.currentMinute = newMinute;
    }

    event.preventDefault();
  }

  monthDropdownChange(target: any): void {
    this.onMonthDropdownChange(target.value);
  }

  onUserInput(event: any) {
    if (!this.isKeydown) {
      return;
    }
    this.isKeydown = false;

    let val = event.target.value;
    try {
      let value = this.parseValueFromString(val);

      if (value && !Array.isArray(value) && moment(value).isValid()) {
        if (!Array.isArray(value)) {
          if (this.minDate && moment(value).isBefore(moment(this.minDate))) {
            value = moment(this.minDate).clone().toDate();
          }

          if (this.maxDate && moment(value).isAfter(moment(this.maxDate))) {
            value = moment(this.maxDate).clone().toDate();
          }
        }
      }

      if (this.isValidSelection(value)) {
        this.updateModel(value);
        this.updateUI();
      }
    } catch (err) {
      //invalid date
      this.updateModel(null);
    }

    this.filled = val != null && val.length;
    this.onInput.emit(event);
  }

  parseDateTime(text: string): Date {
    let date: Date;
    let parts: string[] = text.split(' ');

    if (this.timeOnly) {
      const is24Hour = parts[0] && parts[0].includes('24:');

      if (this.show24Hour && is24Hour) {
        date = this.dayForTime ? moment(this.dayForTime).add(1, 'd').toDate() : new Date();

        this.populateTime(date, parts[0].replace('24:', '00:'), parts[1]);
      } else {
        date = this.dayForTime ? moment(this.dayForTime).toDate() : new Date();
        this.populateTime(date, parts[0], parts[1]);
      }
    } else {
      const dateFormat = this.getDateFormat();
      if (this.showTime) {
        let ampm = this.hourFormat == '12' ? parts.pop() : null;
        let timeString = parts.pop();

        date = this.parseDate(parts.join(' '), dateFormat);
        this.populateTime(date, timeString, ampm);
      } else {
        date = this.parseDate(text, dateFormat);
      }
    }

    return date;
  }

  setCurrentHourPM(hours: number) {
    if (this.hourFormat == '12') {
      this.pm = hours > 11;
      if (hours >= 12) {
        this.currentHour = hours == 12 ? 12 : hours - 12;
      } else {
        this.currentHour = hours == 0 ? 12 : hours;
      }
    } else {
      if (this.show24Hour && this.dayForTime) {
        if (moment(this.value).isAfter(moment(this.dayForTime).endOf('day'))) {
          this.currentHour = 24;

          return;
        }
      }

      this.currentHour = hours;
    }
  }

  updateTime() {
    let value = this.value;
    if (this.isRangeSelection()) {
      value = this.value[1] || this.value[0];
    }
    if (this.isMultipleSelection()) {
      value = this.value[this.value.length - 1];
    }
    value = value ? new Date(value.getTime()) : new Date();

    if (this.dayForTime) {
      value = moment(this.dayForTime).clone().toDate();
    }

    if (this.hourFormat == '12') {
      if (this.currentHour === 12) value.setHours(this.pm ? 12 : 0);
      else value.setHours(this.pm ? this.currentHour + 12 : this.currentHour);
    } else {
      value.setHours(this.currentHour);
    }

    value.setMinutes(this.currentMinute);
    value.setSeconds(this.currentSecond);
    if (this.isRangeSelection()) {
      if (this.value[1]) value = [this.value[0], value];
      else value = [value, null];
    }

    if (this.isMultipleSelection()) {
      value = [...this.value.slice(0, -1), value];
    }

    this.updateModel(value);
    this.onSelect.emit(value);
    this.updateInputfield();
  }

  yearDropdownChange(target: any): void {
    this.onYearDropdownChange(target.value);
  }

  validateTime(hour: number, minute: number, second: number, pm: boolean) {
    let value = this.value;
    let convertedHour = this.convertTo24Hour(hour, pm);
    if (this.isRangeSelection()) {
      value = this.value[1] || this.value[0];
    }
    if (this.isMultipleSelection()) {
      value = this.value[this.value.length - 1];
    }

    if (this.dayForTime) {
      value = moment(this.dayForTime).clone().toDate();
    }

    if (hour === 24) {
      value = moment(value).add(1, 'd').toDate();
      convertedHour = 0;
      hour = 0;
    }

    const minIsBeforeNow = this.minDate ? moment(this.minDate).isBefore(moment()) : false;
    const valueDateString = value ? value.toDateString() : minIsBeforeNow ? this.minDate.toDateString() : new Date().toDateString();

    if (this.minDate) {
      if (valueDateString && this.minDate.toDateString() === valueDateString) {
        if (this.minDate.getHours() > convertedHour) {
          return false;
        }
        if (this.minDate.getHours() === convertedHour) {
          if (this.minDate.getMinutes() > minute) {
            return false;
          }
          if (this.minDate.getMinutes() === minute) {
            if (this.minDate.getSeconds() > second) {
              return false;
            }
          }
        }
      } else if (valueDateString) {
        if (moment(value).isBefore(moment(this.minDate))) {
          return false;
        }
      }
    }

    if (this.maxDate) {
      if (valueDateString && this.maxDate.toDateString() === valueDateString) {
        if (this.maxDate.getHours() < convertedHour) {
          return false;
        }
        if (this.maxDate.getHours() === convertedHour) {
          if (this.maxDate.getMinutes() < minute) {
            return false;
          }
          if (this.maxDate.getMinutes() === minute) {
            if (this.maxDate.getSeconds() < second) {
              return false;
            }
          }
        }
      } else if (valueDateString) {
        if (moment(value).isAfter(moment(this.maxDate))) {
          return false;
        }
      }
    }
    return true;
  }
}
