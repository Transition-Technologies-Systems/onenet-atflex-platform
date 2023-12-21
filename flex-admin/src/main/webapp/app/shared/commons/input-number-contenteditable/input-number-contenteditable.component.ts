import { Subject } from 'rxjs';
import {
  Component,
  OnInit,
  ViewChild,
  Output,
  EventEmitter,
  ChangeDetectorRef,
  ChangeDetectionStrategy,
  OnDestroy,
  Input,
  AfterViewInit,
  ElementRef,
  Self,
  Optional,
  Renderer2,
} from '@angular/core';
import { ControlValueAccessor, NgControl } from '@angular/forms';

@Component({
  selector: 'app-input-number-contenteditable',
  templateUrl: './input-number-contenteditable.component.html',
  styleUrls: ['./input-number-contenteditable.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: {
    class: 'p-element p-inputwrapper',
    '[class.p-inputwrapper-filled]': 'filled',
    '[class.p-inputwrapper-focus]': 'focused || overlayVisible',
    '[class.p-inputnumber-clearable]': 'showClear && buttonLayout != "vertical"',
  },
})
export class InputNumberContenteditableComponent implements OnInit, OnDestroy, AfterViewInit, ControlValueAccessor {
  @Input() showButtons = false;
  @Input() buttonLayout = 'stacked';
  @Input() suffix = '';
  @Input() negative = false;
  @Input() disabled = false;
  @Input() correctValue = true;
  @Input() min: number | undefined;
  @Input() max: number | undefined;
  @Input() step: number = 1;
  @Input() maxlength: number | undefined;
  @Input() inputId = 'field-input-number';
  @Input() maxInteger: number | undefined;
  @Input() minFractionDigits = 0;
  @Input() maxFractionDigits = 0;
  @Input() contentEditable = true;
  @Input() showValue = false;
  @ViewChild('input') input!: ElementRef;

  @Output() onChange = new EventEmitter<any>();

  value: any;

  focused = false;
  filled = false;
  overlayVisible = false;

  regexp: RegExp | undefined;

  numberFormat = new Intl.NumberFormat('pl', this.getOptions());

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
  constructor(private renderer: Renderer2, private cdr: ChangeDetectorRef, @Optional() @Self() public ngControl: NgControl) {
    if (this.ngControl != null) {
      this.ngControl.valueAccessor = this;
      this.cdr.markForCheck();
    }
  }

  ngAfterViewInit(): void {
    this.numberFormat = new Intl.NumberFormat('pl', this.getOptions());
    if (this.input && (this.value || this.value === 0)) {
      const div = this.input.nativeElement;
      this.renderer.setProperty(div, 'textContent', this.numberFormat.format(this.value));
    }
  }

  ngOnInit(): void {
    this.stateChanges.subscribe(() => this.cdr.markForCheck());
    this.regexp = new RegExp(
      `^${this.negative ? '-?' : ''}\\d{1,${this.maxInteger}}(\\,\\d{${this.minFractionDigits},${this.maxFractionDigits}})?$`
    );
  }

  ngOnDestroy(): void {
    this.stateChanges.complete();
  }

  onChangeValue(value: any): void {
    this.emitChangedValues(value);
  }

  onInput(event: any): void {
    const value = event?.target.textContent.replace(',', '.').replace(/\s/g, '');
    const emitValue = isNaN(parseFloat(value)) ? null : parseFloat(value);
    this.value = emitValue;
    this.emitChangedValues(emitValue);
  }

  getDecimalSignPosition(text: string): number {
    return text.indexOf(',');
  }

  onKeyDown(event: any): any {
    if (this.disabled) {
      event.preventDefault();
      return false;
    }
    const { which, keyCode } = event;
    const charCode = which ?? keyCode;
    const value = event?.target.textContent;
    const sel = window.getSelection() as any;

    const checkValue = value.slice(0, sel.baseOffset) + event.key + value.slice(sel.extentOffset);

    const decimalSignIndex = this.getDecimalSignPosition(checkValue);
    if (charCode === 8) {
      const caretPosition = sel.focusOffset - 1;
      if (value.charCodeAt(caretPosition) === 44) {
        this.setCaret(event, sel, caretPosition);
        return false;
      }

      if (decimalSignIndex > -1 && sel.focusOffset > decimalSignIndex + 1) {
        if (sel.type === 'Range') {
          return true;
        }
        const newValue = value.slice(0, sel.baseOffset - 1) + 0 + value.slice(sel.extentOffset);

        event.target.textContent = newValue;
        this.setCaret(event, sel, caretPosition);
        this.onInput(event);
        return false;
      }
    }

    if (charCode === 38) {
      const carretPosition = sel.focusOffset;

      const newValue = this.naiveRound(this.value + this.step, this.maxFractionDigits);
      if (this.maxInteger && this.maxInteger < newValue.toString().length) {
        return false;
      }
      this.value = newValue;
      event.target.textContent = this.numberFormat.format(newValue);
      this.setCaret(event, sel, carretPosition);
      this.onInput(event);
      return false;
    }

    if (charCode === 40) {
      const carretPosition = sel.focusOffset;
      let newValue = this.naiveRound(this.value - this.step, this.maxFractionDigits);
      if (this.maxInteger && this.maxInteger < newValue.toString().length) {
        return false;
      }

      if (!this.negative && newValue <= 0) {
        newValue = 0;
      }

      this.value = newValue;
      event.target.textContent = this.numberFormat.format(newValue);
      this.setCaret(event, sel, carretPosition);
      this.onInput(event);
      return false;
    }
  }

  onKeyPress(event: any): any {
    if (this.disabled) {
      event.preventDefault();
      return false;
    }
    const { which, keyCode } = event;
    const charCode = which ?? keyCode;
    const value = event?.target.textContent;
    const sel = window.getSelection() as any;

    let checkValue = value.slice(0, sel.baseOffset) + event.key + value.slice(sel.extentOffset);

    if (sel.type === 'Range') {
      const min = Math.min(sel.extentOffset, sel.baseOffset);
      const max = Math.max(sel.extentOffset, sel.baseOffset);
      checkValue = value.slice(0, min) + event.key + value.slice(max);
    }

    const decimalSignIndex = this.getDecimalSignPosition(checkValue);
    if (decimalSignIndex === -1 && checkValue !== '-' && this.matchRegex(checkValue)) {
      const carretPosition = checkValue.includes('-') ? 2 : 1;
      event.target.textContent = this.numberFormat.format(checkValue);
      this.setCaret(event, sel, carretPosition);
      this.onInput(event);
      this.ngTouched();
      return false;
    }

    if (decimalSignIndex > -1 && sel.focusOffset > decimalSignIndex && charCode >= 48 && charCode <= 57) {
      const carretPosition = sel.focusOffset + 1;
      const oldValue = event.target.textContent;
      event.target.textContent = this.setCharAt(event.target.textContent, sel.focusOffset, String.fromCharCode(charCode));
      if (oldValue !== event.target.textContent) {
        this.setCaret(event, sel, carretPosition);
      }
      this.onInput(event);
      this.ngTouched();
      return false;
    }

    if (this.negative && checkValue === '-' && charCode === 45) {
      return true;
    }

    if (value && charCode === 44) {
      if (value.includes(',')) {
        if (event.target.textContent.charCodeAt(sel.focusOffset) === charCode) {
          const carretPosition = sel.focusOffset + 1;
          this.setCaret(event, sel, carretPosition);
        }
        return false;
      } else {
        return true;
      }
    }

    return this.matchRegex(checkValue);
  }

  onBlur(event: any): void {
    const value = parseFloat(event.target.textContent.replace(',', '.').replace(/\s/g, ''));
    event.target.textContent = isNaN(value) ? null : this.numberFormat.format(value);
    this.writeValue(value);
  }

  reverseFormat(value: string): string {
    return value.replace(/\s/g, '').replace('.', ',');
  }

  onFocus(event: any): void {
    if (this.disabled) {
      event.preventDefault();
      return;
    }
    event.target.textContent = this.reverseFormat(event.target.textContent);
    this.focused = true;
  }

  onPaste(event: any): boolean {
    const value = event.clipboardData.getData('text');
    return this.matchRegex(value);
  }

  writeValue(value: any): void {
    if (this.input && (value || value === 0)) {
      const div = this.input.nativeElement;
      this.renderer.setProperty(div, 'textContent', this.numberFormat.format(value));
    }
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

  matchRegex(value: string): boolean {
    if (this.regexp) {
      return this.regexp.test(String(value));
    }
    return true;
  }

  private emitChangedValues(value: any): void {
    this.filled = this.isFilled(value);
    this.ngChange(value);
    this.onChange.emit(value);
  }

  private isFilled(value: any): boolean {
    return !!value || value === 0;
  }

  private setCharAt(str: string, index: number, char: string): string {
    if (index > str.length - 1 && str.length - str.indexOf(',') > this.maxFractionDigits) {
      return str;
    }
    return str.substring(0, index) + char + str.substring(index + 1);
  }

  private setCaret(event: any, selection: any, position: number): void {
    const range = document.createRange();
    range.selectNodeContents(event.target);
    range.setStart(event.target.firstChild, position);
    range.setEnd(event.target.firstChild, position);
    selection.removeAllRanges();
    selection.addRange(range);
    event.target.focus();
    range.detach(); // optimization

    // set scroll to the end if multiline
    event.target.scrollTop = event.target.scrollHeight;
  }

  setDisabledState(isDisabled: boolean): void {
    this.disabled = isDisabled;
    this.stateChanges.next();
  }

  naiveRound(num: number, decimalPlaces = 0) {
    var p = Math.pow(10, decimalPlaces);
    return Math.round(num * p) / p;
  }

  private getOptions(): any {
    return {
      style: 'decimal',
      useGrouping: true,
      minimumFractionDigits: this.minFractionDigits,
      maximumFractionDigits: this.maxFractionDigits,
    };
  }
}
