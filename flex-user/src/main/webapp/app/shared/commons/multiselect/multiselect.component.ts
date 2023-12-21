import { MultiSelect } from 'primeng/multiselect';
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
  ViewChild,
} from '@angular/core';
import { ControlValueAccessor, NgControl } from '@angular/forms';
import { Dictionary } from '@app/shared/models';
import { TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-multiselect',
  templateUrl: './multiselect.component.html',
  styleUrls: ['./multiselect.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,

  // tslint:disable-next-line:no-host-metadata-property
  host: {
    '[class.p-inputwrapper-filled]': 'filled',
    '[class.p-inputwrapper-focus]': 'focused || overlayVisible',
  },
})
export class MultiselectComponent implements ControlValueAccessor, OnInit, OnDestroy, OnChanges {
  @Input() styleClass = 'multiselect-style';
  @Input() iconClass = 'icon icon--expand';
  @Input() translateDictionaries = true;
  @Input('options') set options(value: any[] | null) {
    this._options = value;
    this.sortOptions();
  }

  @Input() showHeader = true;
  @Input() inputId: string | undefined;

  @Input() optionObject = false;
  @Input() optionValue = 'value';
  @Input() optionLabel = 'label';

  @ViewChild(MultiSelect) multiselect: MultiSelect | undefined;

  // tslint:disable-next-line:no-output-on-prefix
  @Output() onChange = new EventEmitter<any>();

  value: any[] = [];

  focused = false;
  filled = false;
  disabled = false;
  overlayVisible = false;
  selectedOptions: any[] = [];
  _options: any[] | null = [];

  private defaultValue: any[] | null = null;
  private stateChanges = new Subject<void>();
  private ngChange = (value: any) => {};
  private ngTouched = () => {};
  private destroy$ = new Subject<void>();

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

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.options) {
      this.sortOptions();
      const hasPreviousValue = Array.isArray(changes.options.previousValue) && changes.options.previousValue.length;
      const hasCurrentValue = Array.isArray(changes.options.currentValue) && changes.options.currentValue.length;

      if (!hasPreviousValue && hasCurrentValue) {
        setTimeout(() => this.onChangeOptions());
      }
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.stateChanges.complete();
  }

  getDictionaryName(items: any[]): any[] {
    if (!items || !items.length) {
      return [];
    }

    let selectedOptions: any[] = [];
    if (this._options) {
      items.forEach(item => {
        let xd = this._options?.find(x => x[this.optionValue] === item);
        selectedOptions.push(xd);
      });
    }

    return selectedOptions;
  }

  onChangeValue(event: { value: any[] }): void {
    let option = event.value;

    if (this.optionObject && event.value) {
      option = this._options?.filter((data: Dictionary) => event.value.includes(data[this.optionValue])) ?? [];
    }

    this.ngChange(option);

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

  show(): void {
    this.multiselect?.show();
    this.multiselect?.cd?.markForCheck();
  }

  writeValue(value: any): void {
    let option = value;

    if (this.optionObject && value) {
      option = value.map((data: Dictionary) => data[this.optionValue]);
    }

    this.filled = option !== null && option?.length;
    this.value = option;
    this.defaultValue = option;

    this.stateChanges.next();
  }

  registerOnChange(fn: any): void {
    this.ngChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.ngTouched = fn;
  }

  private onChangeOptions(): void {
    if (Array.isArray(this.defaultValue) && this.defaultValue.length) {
      this.value = this.defaultValue;

      const selected = this.defaultValue.map(value => (typeof value === 'object' ? value[this.optionValue] : value));
      this.selectedOptions = this._options?.filter((value: any) => selected.includes(value[this.optionValue])) ?? [];

      this.defaultValue = null;

      this.stateChanges.next();
    }
  }

  private sortOptions(): void {
    this._options =
      this._options?.sort((a, b) => {
        if (a[this.optionLabel] && b[this.optionLabel]) {
          return this.translate.instant(a[this.optionLabel]).localeCompare(this.translate.instant(b[this.optionLabel]));
        }
        return -1;
      }) ?? [];
  }
}
