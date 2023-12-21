import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';

import { Component } from '@angular/core';

@Component({
  selector: 'app-confirm-modal',
  templateUrl: './confirm-modal.component.html',
})
export class ConfirmModalComponent {
  constructor(public ref: DynamicDialogRef, public config: DynamicDialogConfig) {}

  close(): void {
    this.ref.close(false);
  }

  confirm(): void {
    this.ref.close(true);
  }
}
