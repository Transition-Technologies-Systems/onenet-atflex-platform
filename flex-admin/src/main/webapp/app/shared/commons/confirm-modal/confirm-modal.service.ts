import { DynamicDialogRef } from 'primeng/dynamicdialog';

import { Injectable } from '@angular/core';

import { ModalService } from '../modal/modal.service';
import { ConfirmModalComponent } from './confirm-modal.component';

@Injectable()
export class ConfirmModalService {
  constructor(private modalSerivce: ModalService) {}

  open(question: string, params: object = {}, translations: { title?: string; btn?: string } = {}): DynamicDialogRef {
    const { title, btn } = translations;

    return this.modalSerivce.open(ConfirmModalComponent, {
      data: {
        question,
        params,
        title,
        btn,
      },
    });
  }
}
