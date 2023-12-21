import { ChangeDetectionStrategy, ChangeDetectorRef, Component, EventEmitter, Input, Optional, Output, Self } from '@angular/core';
import { ControlValueAccessor, NgControl } from '@angular/forms';
import { Dictionary } from '@app/shared/models';

@Component({
  selector: 'app-version-choose',
  templateUrl: './version-choose.component.html',
  styleUrls: ['./version-choose.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class VersionChooseComponent implements ControlValueAccessor {
  @Input() versions: Dictionary[] | null = [];
  @Output() closePreview = new EventEmitter<void>();

  version = 0;

  private ngChange = (value: any) => {};
  private ngTouched = () => {};

  constructor(private cdr: ChangeDetectorRef, @Optional() @Self() public ngControl: NgControl) {
    if (this.ngControl != null) {
      this.ngControl.valueAccessor = this;
    }
  }

  close(): void {
    this.closePreview.emit();
  }

  writeValue(value: number): void {
    this.version = value;
    this.cdr.markForCheck();
  }

  registerOnChange(fn: any): void {
    this.ngChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.ngTouched = fn;
  }

  versionChanged(value: number): void {
    this.version = value;
    this.ngChange(value);
  }
}
