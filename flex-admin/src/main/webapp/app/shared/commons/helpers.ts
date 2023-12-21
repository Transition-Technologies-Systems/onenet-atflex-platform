import { AbstractControl, UntypedFormArray, UntypedFormControl, UntypedFormGroup, ValidatorFn } from '@angular/forms';
import { ChangeTime } from '../enums';
import { NgZone } from '@angular/core';
import { isMoment } from 'moment';

import { Dictionary } from '../models';
import { moment } from 'polyfills';
import { range } from 'lodash-es';
import { take } from 'rxjs/operators';

export class Helpers {
  /**
   * Capitalize string
   */
  static capitalize(s: string): string {
    return s.charAt(0).toUpperCase() + s.slice(1);
  }

  /**
   * Check change time in date range
   */
  static checkChangeTime(startDate: moment.Moment, endDate: moment.Moment): ChangeTime {
    let changeDay = null;

    const dateFrom = moment(startDate);
    const dateTo = moment(endDate);

    const firstDay = moment(dateFrom.format()).set({ h: 0 }).utcOffset();
    const lastDay = moment(dateTo.format()).set({ h: 23 }).utcOffset();

    const days = dateTo.diff(dateFrom, 'days');

    if (firstDay !== lastDay) {
      for (let d = days; d > 0; d--) {
        const dayOffset = dateFrom.clone().set({ h: 0 }).add(d, 'days').utcOffset();
        const endDayOffset = dateFrom.clone().set({ h: 23 }).add(d, 'days').utcOffset();

        if (dayOffset !== endDayOffset) {
          changeDay = dateFrom.clone().set({ h: 0 }).add(d, 'days').date();
          break;
        }
      }
    }

    return {
      hours: firstDay === lastDay ? 24 : firstDay > lastDay ? 25 : 23,
      isWinterChangeTime: firstDay > lastDay,
      isSummerChangeTime: firstDay < lastDay,
      changeDay,
    };
  }

  /**
   * Disable form controls
   *
   * @param form The form group or form array
   * @param fields The fields to disabled
   * @param clearValue Clear value when disabled
   */
  static changeFormControlsState(
    form: UntypedFormGroup | UntypedFormArray,
    controls: string[],
    type: 'DISABLE' | 'ENABLE',
    clearValue: boolean = true
  ): void {
    const setDisable = (formGroup: UntypedFormGroup): void => {
      controls.forEach((control: string) => {
        const fieldControl = formGroup.get(control) as UntypedFormControl;

        if (type === 'DISABLE') {
          fieldControl?.disable();

          if (clearValue) {
            fieldControl?.setValue(null);
            fieldControl?.updateValueAndValidity();
          }
        } else {
          fieldControl?.enable();
        }
      });
    };

    if (form instanceof UntypedFormArray) {
      form.controls.forEach((control: AbstractControl) => setDisable(control as UntypedFormGroup));
    } else {
      setDisable(form);
    }
  }

  static createKeys(keys: string[], suffixs: string[]): string[] {
    return keys.reduce((currentKeys: string[], key: string) => {
      return [...currentKeys, ...suffixs.map((suffix: string) => `${key}${this.capitalize(suffix)}`)];
    }, []);
  }

  /**
   * Create dictionary from enum
   *
   * @param data The enum
   * @param nlsCodePrefix The nlsCode prefix
   */
  static enumToDictionary<T = string | number | boolean>(
    data: any,
    nlsCodePrefix?: string,
    customFilters?: (value: string) => boolean
  ): Dictionary<T>[] {
    const mapToDictionaries = (value: T) => ({
      value: value as T,
      label: nlsCodePrefix ? `${nlsCodePrefix}.${value}` : `${value}`,
    });

    const dictionaryData = Array.isArray(data) ? data : Object.values(data);

    if (customFilters) {
      return dictionaryData.filter(customFilters).map(mapToDictionaries);
    }

    return dictionaryData.map(mapToDictionaries);
  }

  /**
   * Some every
   */
  static async everyAsync<T>(array: T[], callbackfn: (value: T, index: number, array: T[]) => Promise<boolean>): Promise<boolean> {
    const filterMap = await Helpers.mapAsync(array, callbackfn);

    return array.every((_, index) => filterMap[index]);
  }

  /**
   * Format file size
   *
   * @param bytes The file size
   * @returns file size format
   */
  static formatSize(bytes: number): string {
    if (bytes === 0) {
      return '0 B';
    }

    const k = 1024;
    const dm = 3;
    const sizes = ['B', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));

    return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + ' ' + sizes[i];
  }

  /**
   * Get hours for changeTime object
   */
  static getHours(changeTime: ChangeTime): string[] {
    const additionalHour = 2;

    const hours = Array.from({ length: 24 }, (_, i) => String(i + 1));

    if (changeTime.isWinterChangeTime) {
      return [...hours.slice(0, additionalHour), `${additionalHour}a`, ...hours.slice(additionalHour)];
    }

    return hours;
  }

  /**
   * Check if value is not empty
   *
   * @param value any value
   */
  static isFilled(value: any): boolean {
    return typeof value === 'number' ? true : !!value;
  }

  /**
   * Check value is empty
   */
  static isNill(value: any): boolean {
    return !value && value !== 0;
  }

  /**
   * Map assync array
   */
  static mapAsync<T, U>(array: T[], callbackfn: (value: T, index: number, array: T[]) => Promise<U>): Promise<U[]> {
    return Promise.all(array.map(callbackfn));
  }

  /**
   * Remove form group
   *
   * @param form The form array
   * @param elements The number of elements from new data
   */
  static removeExcessFormGroup(form: UntypedFormArray, elements: number): void {
    if (form.value.length <= elements) {
      return;
    }

    range(form.value.length, elements, -1).forEach((index: number) => {
      form.removeAt(index - 1);
    });
  }

  /**
   * Run function when zone is stabl
   *
   * @param ngZone The NgZone instance
   * @param callback The callback function
   */
  static runOnStableZone(ngZone: NgZone, callback: () => void): void {
    if (ngZone.isStable) {
      callback();
    } else {
      ngZone.onStable.pipe(take(1)).subscribe(() => callback());
    }
  }

  /**
   * Set validator to controls
   */
  static setValidatorToControls(form: UntypedFormGroup | UntypedFormArray, controls: string[], validators: ValidatorFn[]): void {
    const setValidator = (formGroup: UntypedFormGroup): void => {
      controls.forEach((control: string) => {
        const fieldControl = formGroup.get(control) as UntypedFormControl;

        if (validators.length) {
          fieldControl?.setValidators(validators);
        } else {
          fieldControl?.clearValidators();
        }

        fieldControl?.updateValueAndValidity();
      });
    };

    if (form instanceof UntypedFormArray) {
      form.controls.forEach((control: AbstractControl) => setValidator(control as UntypedFormGroup));
    } else {
      setValidator(form);
    }
  }

  /**
   * Serialize filters for ngrx
   */
  static serializeFilters(
    filtersForm: object,
    filtersTable: object = {},
    dateTimePath: string[] = ['createdDate', 'lastModifiedDate']
  ): object {
    const formatValue = (value: any, key: string): any => {
      const [dateKey] = key.split('.');
      const format = dateTimePath.includes(dateKey) ? undefined : 'YYYY-MM-DD';

      if (isMoment(value)) {
        return format ? value.format(format) : value.utc().format();
      } else if (value instanceof Date) {
        return format ? moment(value).format(format) : moment(value).utc().format();
      }

      return value;
    };

    const serializeData = (filters: any): object => {
      let data: { [key: string]: any } = {};

      Object.keys(filters).forEach((key: string): void => {
        let value = filters[key];

        if (value && Array.isArray(value)) {
          value = value.map((val: any) => formatValue(val, key));
        } else if (value && !(value instanceof Date || moment.isMoment(value)) && typeof value === 'object') {
          data[key] = serializeData(value);

          return;
        }

        data = {
          ...data,
          [key]: formatValue(value, key),
        };
      });

      return data;
    };

    const form = serializeData(filtersForm);
    const table = serializeData(filtersTable);

    const tableKeys = Object.keys(table);

    return Object.entries(form).reduce(
      (previousValue: any, [key, value]) => {
        let filterValue = value;

        if (tableKeys.includes(key)) {
          if (Array.isArray(previousValue[key])) {
            filterValue = [...previousValue[key], value];
          } else {
            filterValue = [previousValue[key], value];
          }

          filterValue = filterValue.filter((filterVal: any) => !!filterVal);
        }

        return {
          ...previousValue,
          [key]: filterValue,
        };
      },
      { ...table }
    );
  }

  /**
   * Some async
   */
  static async someAsync<T>(array: T[], callbackfn: (value: T, index: number, array: T[]) => Promise<boolean>): Promise<boolean> {
    const filterMap = await Helpers.mapAsync(array, callbackfn);

    return array.some((value, index) => filterMap[index]);
  }
}
