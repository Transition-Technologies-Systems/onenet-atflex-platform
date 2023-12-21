import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { Subject } from 'rxjs';

import { Component, OnDestroy } from '@angular/core';

@Component({
  template: '',
})
// tslint:disable-next-line:component-class-suffix
export abstract class DialogExtends implements OnDestroy {
  mode: 'add' | 'edit' = 'add';

  protected destroy$ = new Subject<void>();

  abstract save(): void;

  constructor(public ref: DynamicDialogRef, public config: DynamicDialogConfig) {
    this.mode = this.config.data?.id ? 'edit' : 'add';
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  confirmClosableActions = (confirm: boolean): void => {
    if (!!confirm) {
      this.save();
    }
  };

  close(result: boolean = false): void {
    this.ref.close(result);
  }
}
