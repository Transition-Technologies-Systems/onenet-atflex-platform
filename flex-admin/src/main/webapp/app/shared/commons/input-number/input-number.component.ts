import { InputNumber } from 'primeng/inputnumber';
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

@Component({
  selector: 'app-input-number',
  templateUrl: './input-number.component.html',
  styleUrls: ['./input-number.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,

  // tslint:disable-next-line:no-host-metadata-property
  host: {
    '[class.p-inputwrapper-filled]': 'filled',
    '[class.p-inputwrapper-focus]': 'focused || overlayVisible',
  },
})
export class InputNumberComponent implements ControlValueAccessor, OnInit, OnDestroy, AfterViewInit {
  @Input() suffix = '';
  @Input() negative = false;
  @Input() disabled = false;
  @Input() correctValue = true;
  @Input() min: number | undefined;
  @Input() max: number | undefined;
  @Input() maxlength: number | undefined;
  @Input() inputId = 'field-input-number';
  @Input() maxInteger: number | undefined;
  @Input() minFractionDigits: number | null = null;
  @Input() maxFractionDigits: number | null = null;

  @ViewChild(InputNumber) inputNumber: InputNumber | undefined;

  // tslint:disable-next-line:no-output-on-prefix
  @Output() onChange = new EventEmitter<any>();

  value: any[] = [];

  focused = false;
  filled = false;
  overlayVisible = false;

  get maxLengthValue(): number {
    return this.maxlength ? this.maxlength + 1 : (this.maxlength as number);
  }

  get maxValue(): number {
    return this.max as number;
  }

  get maxFractionDigitsValue(): number {
    return this.maxFractionDigits as number;
  }

  get minValue(): number {
    if (!this.negative) {
      return Math.max(0, this.min || 0);
    }

    return this.min as number;
  }

  get minFractionDigitsValue(): number {
    return this.minFractionDigits as number;
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
    if (!this.inputNumber) {
      return;
    }

    this.inputNumber.isMinusSign = char => {
      const minusSign = this.inputNumber?.getMinusSignExpression();

      if (!this.negative || !minusSign) {
        return false;
      }

      if (minusSign.test(char)) {
        minusSign.lastIndex = 0;
        return true;
      }

      return false;
    };

    this.inputNumber.updateInput = (value, insertedValueStr, operation, valueStr) => {
      insertedValueStr = insertedValueStr || '';

      if (!this.inputNumber) {
        return;
      }

      const inputValue = this.inputNumber.input.nativeElement.value;
      let newValue = this.inputNumber.formatValue(value);
      const currentLength = inputValue.length;

      if (newValue !== valueStr) {
        newValue = this.inputNumber.concatValues(newValue, valueStr);
      }

      const isMinusSignStart = inputValue.includes('-');
      const minusSignLength = isMinusSignStart ? 1 : 0;
      if (this.maxInteger && !['delete-single', 'delete-range', 'delete-back-single'].includes(operation)) {
        const decimalCharIndex = newValue.search(this.inputNumber.getDecimalExpression());
        const integerValue = newValue.substring(0, decimalCharIndex > -1 ? decimalCharIndex : newValue.length).replace(/\s/g, '');
        if (integerValue.length > this.maxInteger + minusSignLength) {
          return;
        }
      }

      if (currentLength === 0) {
        this.inputNumber.input.nativeElement.value = newValue;
        this.inputNumber.input.nativeElement.setSelectionRange(0, 0);
        const index = this.inputNumber.initCursor();
        const selectionEnd = index + insertedValueStr.length;
        this.inputNumber.input.nativeElement.setSelectionRange(selectionEnd, selectionEnd);
      } else {
        const selectionStart = this.inputNumber.input.nativeElement.selectionStart;
        let selectionEnd = this.inputNumber.input.nativeElement.selectionEnd;
        if (this.maxlength && this.maxlength < newValue.length) {
          return;
        }

        this.inputNumber.input.nativeElement.value = newValue;
        const newLength = newValue.length;

        if (operation === 'range-insert') {
          const startValue = this.inputNumber.parseValue((inputValue || '').slice(0, selectionStart));
          const startValueStr = startValue !== null ? startValue.toString() : '';
          const startExpr = startValueStr.split('').join(`(${this.inputNumber.groupChar})?`);
          const sRegex = new RegExp(startExpr, 'g');
          sRegex.test(newValue);

          const tExpr = insertedValueStr.split('').join(`(${this.inputNumber.groupChar})?`);
          const tRegex = new RegExp(tExpr, 'g');
          tRegex.test(newValue.slice(sRegex.lastIndex));

          selectionEnd = sRegex.lastIndex + tRegex.lastIndex;
          this.inputNumber.input.nativeElement.setSelectionRange(selectionEnd, selectionEnd);
        } else if (newLength === currentLength) {
          if (operation === 'insert' || operation === 'delete-back-single') {
            this.inputNumber.input.nativeElement.setSelectionRange(selectionEnd + 1, selectionEnd + 1);
          } else if (operation === 'delete-single') {
            this.inputNumber.input.nativeElement.setSelectionRange(selectionEnd - 1, selectionEnd - 1);
          } else if (operation === 'delete-range' || operation === 'spin') {
            this.inputNumber.input.nativeElement.setSelectionRange(selectionEnd, selectionEnd);
          }
        } else if (operation === 'delete-back-single') {
          const prevChar = inputValue.charAt(selectionEnd - 1);
          const nextChar = inputValue.charAt(selectionEnd);
          const diff = currentLength - newLength;
          const isGroupChar = this.inputNumber._group.test(nextChar);

          if (isGroupChar && diff === 1) {
            selectionEnd += 1;
          } else if (!isGroupChar && this.inputNumber.isNumeralChar(prevChar)) {
            selectionEnd += -1 * diff + 1;
          }

          this.inputNumber._group.lastIndex = 0;
          this.inputNumber.input.nativeElement.setSelectionRange(selectionEnd, selectionEnd);
        } else if (inputValue === '-' && operation === 'insert') {
          this.inputNumber.input.nativeElement.setSelectionRange(0, 0);
          const index = this.inputNumber.initCursor();
          const selectionEnd = index + insertedValueStr.length + 1;
          this.inputNumber.input.nativeElement.setSelectionRange(selectionEnd, selectionEnd);
        } else {
          selectionEnd = selectionEnd + (newLength - currentLength);
          this.inputNumber.input.nativeElement.setSelectionRange(selectionEnd, selectionEnd);
        }
      }

      this.inputNumber.input.nativeElement.setAttribute('aria-valuenow', value);
    };

    this.inputNumber.validateValue = value => {
      if (value === '-' || value == null) {
        return null;
      }

      if (!this.negative && value < 0) {
        return 0;
      }

      if (!this.correctValue) {
        return value;
      }

      if (value || value === 0) {
        if (this.min != null && value < this.min) {
          return this.min;
        }

        if (this.max != null && value > this.max) {
          return this.max;
        }
      }

      return value;
    };
  }

  onChangeValue(value: any): void {
    this.emitChangedValues(value);
  }

  onInput(event: any): void {
    this.emitChangedValues(event?.value ?? null);
  }

  private emitChangedValues(value: any) {
    this.filled = this.isFilled(value);
    this.ngChange(value);
    this.onChange.emit(value);
  }

  onBlur(): void {
    this.focused = false;
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
    this.filled = this.isFilled(value);
    this.value = value;

    this.stateChanges.next();
  }

  registerOnChange(fn: any): void {
    this.ngChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.ngTouched = fn;
  }

  private isFilled(value: any): boolean {
    return !!value || value === 0;
  }
}
