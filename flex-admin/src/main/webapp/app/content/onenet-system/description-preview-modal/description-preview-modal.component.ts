import { Component, OnDestroy, OnInit } from '@angular/core';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { Subject } from 'rxjs';

@Component({
  selector: 'app-description-preview-modal',
  templateUrl: './description-preview-modal.component.html',
})
export class DescriptionPreviewModalComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();

  constructor(public ref: DynamicDialogRef, public config: DynamicDialogConfig) {}

  ngOnInit(): void {}

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
