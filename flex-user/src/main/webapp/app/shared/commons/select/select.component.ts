import { Subject, takeUntil, debounceTime, tap } from 'rxjs';

import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnDestroy,
  OnInit,
  Optional,
  Output,
  Self,
  SimpleChanges,
} from '@angular/core';
import { ControlValueAccessor, NgControl } from '@angular/forms';
import { Dictionary } from '@app/shared/models';
import { TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-select',
  templateUrl: './select.component.html',
  styleUrls: ['./select.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,

  // tslint:disable-next-line:no-host-metadata-property
  host: {
    '[class.p-inputwrapper-filled]': 'filled',
    '[class.p-inputwrapper-focus]': 'focused || overlayVisible',
  },
})
export class SelectComponent implements ControlValueAccessor, OnInit, OnDestroy, OnChanges {
  @Input() translateDictionaries = true;
  @Input() styleClass = 'select-style';
  @Input() iconClass = 'icon icon--expand';
  @Input() options: any[] | null = [];
  @Input() showClear = true;
  @Input() inputId: string | undefined;
  @Input() optionWithTooltip = false;
  @Input() optionWithPrompt = false;

  @Input() optionObject = false;
  @Input() optionValue = 'value';
  @Input() optionLabel = 'label';
  @Input() setAsArray = false;
  @Input() sortDictionaries = true;

  // tslint:disable-next-line:no-output-on-prefix
  @Output() onChange = new EventEmitter<any>();

  value: any = null;

  focused = false;
  filled = false;
  disabled = false;
  overlayVisible = false;
  private destroy$ = new Subject<void>();

  private defaultValue: any = null;
  private stateChanges = new Subject<void>();
  private ngChange = (value: any) => {};
  private ngTouched = () => {};

  constructor(private cdr: ChangeDetectorRef, @Optional() @Self() public ngControl: NgControl, private translate: TranslateService) {
    if (this.ngControl != null) {
      this.ngControl.valueAccessor = this;
    }
  }

  ngOnInit(): void {
    this.stateChanges.subscribe(() => this.cdr.markForCheck());
    this.translate.onLangChange
      .pipe(
        takeUntil(this.destroy$),
        debounceTime(500),
        tap(() => this.sortOptions())
      )
      .subscribe();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.stateChanges.complete();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['options']) {
      this.sortOptions();
      const hasPreviousValue = Array.isArray(changes['options'].previousValue) && changes['options'].previousValue.length;
      const hasCurrentValue = Array.isArray(changes['options'].currentValue) && changes['options'].currentValue.length;

      if (!hasPreviousValue && hasCurrentValue) {
        this.onChangeOptions();
      }
    }
  }

  getDictionaryName(item: any): any | null {
    if (!item) {
      return null;
    }

    return this.options?.some(({ value }) => item === value);
  }

  onChangeValue(event: { value: any }): void {
    let option = event.value;

    if (this.optionObject && event.value) {
      option = this.options?.find((data: Dictionary) => data[this.optionValue] === event.value);
    }

    if (this.setAsArray) {
      this.ngChange([option]);
    } else {
      this.ngChange(option);
    }

    this.value = event.value;
    this.filled = option !== null && option !== undefined;

    this.onChange.emit(event);
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
    let option = this.setAsArray && Array.isArray(value) ? value[0] : value;

    if (this.optionObject && value) {
      option = typeof value === 'object' ? value[this.optionValue] : null;
    }

    this.filled = option !== null && option !== undefined;
    this.defaultValue = value;
    this.value = option;

    this.stateChanges.next();
  }

  registerOnChange(fn: any): void {
    this.ngChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.ngTouched = fn;
  }

  private onChangeOptions(): void {
    if (this.defaultValue) {
      setTimeout(() => {
        let value = this.setAsArray
          ? Array.isArray(this.defaultValue) && this.defaultValue.length
            ? this.defaultValue[0]
            : null
          : this.defaultValue;

        if (value) {
          if (this.optionObject) {
            value = typeof this.defaultValue === 'object' ? this.defaultValue[this.optionValue] : null;
          }

          this.value = value;
          this.defaultValue = null;
          this.stateChanges.next();
        }
      });
    }
  }

  private sortOptions(): void {
    if (this.sortDictionaries) {
      const sortedOptions =
        this.options
          ?.filter(item => !item.first)
          .sort((a, b) => {
            if (a[this.optionLabel] && b[this.optionLabel]) {
              return this.translate.instant(a[this.optionLabel]).localeCompare(this.translate.instant(b[this.optionLabel]));
            }
            return -1;
          }) ?? [];
      const firstOptions = this.options?.filter(item => item.first) ?? [];
      this.options = [...firstOptions, ...sortedOptions];
    }
  }
}
