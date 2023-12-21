import { Message, MessageService } from 'primeng/api';

import { Injectable } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';

/**
 * class to build messages for a toastr
 */
export class ToastrMessage {
  /**
   * toastr message
   */
  msg: string;

  /**
   * params to translate
   */
  params?: object;

  /**
   * translate flag
   */
  translate = true;

  constructor(attr: { msg: string; params?: object; translate?: boolean }) {
    this.msg = attr.msg;
    this.params = attr.params;
    this.translate = attr.translate === undefined ? true : attr.translate;
  }
}

/**
 * Available toastr type
 */
export type ToastrType = 'info' | 'success' | 'error' | 'warn';

/**
 * service for displaying a toastr
 */
@Injectable()
export class AppToastrService {
  constructor(private messageService: MessageService, private translateService: TranslateService) {}

  /**
   * Show info toastr
   */
  info(message: string | ToastrMessage, title?: string | undefined, options?: Partial<Message>): void {
    this.show('info', message, title, {
      life: 5000,
      ...options,
    });
  }

  /**
   * Show success toastr
   */
  success(message: string | ToastrMessage, title?: string | undefined, options?: Partial<Message>): void {
    this.show('success', message, title, {
      life: 5000,
      ...options,
    });
  }

  /**
   * Show error toastr
   */
  error(message: string | ToastrMessage, title?: string | undefined, options?: Partial<Message>): void {
    this.show('error', message, title, {
      life: 5000,
      ...options,
    });
  }

  /**
   * Show warning toastr
   */
  warning(message: string | ToastrMessage, title?: string | undefined, options?: Partial<Message>): void {
    this.show('warn', message, title, {
      life: 5000,
      ...options,
    });
  }

  /**
   *
   * @param type toastr type
   * @param message message to be displayed in the toastr - it can be a string or ToastrMessage
   * @param title title to be displayed in the toastr
   * @param override configuration of the toastr display
   */
  private show(type: ToastrType, message: string | ToastrMessage, title?: string, options?: Partial<Message>): void {
    let content = '';

    if (message instanceof ToastrMessage) {
      content = message.translate ? this.translateService.instant(message.msg, message.params) : message.msg;
    } else {
      content = this.translateService.instant(message);
    }

    this.messageService.add({
      severity: type,
      summary: title ? this.translateService.instant(title) : null,
      detail: content,
      ...options,
    });
  }
}
