import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  ElementRef,
  Input,
  OnDestroy,
  OnInit,
  Renderer2,
  ViewChild,
} from '@angular/core';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';

import { ConfirmModalService } from '../confirm-modal/confirm-modal.service';
import { Dialog } from 'primeng/dialog';
import { PrimeNGConfig } from 'primeng/api';

@Component({
  selector: 'app-modal',
  templateUrl: './modal.component.html',
  styleUrls: ['./modal.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ModalComponent implements OnDestroy, OnInit {
  visible = true;

  @ViewChild(Dialog) dialog: Dialog | undefined;

  @Input() confirmClosableActions: ((confirm: boolean) => void) | undefined;
  @Input() showConfirmAfterEsc = true;
  @Input() showCancelBtn = true;
  @Input() closeOnEscape = true;
  @Input() showHeader = false;
  @Input() showFooter = true;
  @Input() closable = false;
  @Input() checkZIndex = true;
  @Input() handyScroll = false;
  @Input() styleClass: string = '';

  get contentStyleClass(): string {
    const classNames = ['app-modal-dialog'];

    if (!this.showHeader) {
      classNames.push('no-header');
    }

    if (!this.showFooter) {
      classNames.push('no-footer');
    }

    if (this.handyScroll) {
      classNames.push('handy-scroll-body');
    }

    return classNames.join(' ');
  }

  private handlerZindex = 0;
  private confirmOpened = false;
  private documentEscapeListener: any;

  constructor(
    public renderer: Renderer2,
    public ref: DynamicDialogRef,
    public elementRef: ElementRef,
    public cdr: ChangeDetectorRef,
    public config: DynamicDialogConfig,
    private primeNgConfig: PrimeNGConfig,
    private confirmService: ConfirmModalService
  ) {}

  ngOnInit(): void {
    this.handlerZindex = (this.config?.baseZIndex || 0) + this.primeNgConfig.zIndex.modal;
    this.listenEscape();
  }

  close(): void {
    this.visible = false;
  }

  onHide(): void {
    this.ref.close();
  }

  ngOnDestroy(): void {
    this.ref.destroy();

    if (this.documentEscapeListener) {
      this.documentEscapeListener();
      this.documentEscapeListener = null;
    }
  }

  private showConfirmClosable(): void {
    if (this.confirmOpened) {
      return;
    }

    if (!this.showConfirmAfterEsc) {
      this.visible = false;
      this.cdr.markForCheck();
      return;
    }

    this.confirmOpened = true;

    this.confirmService.open('confirm.questionCloseModal').onClose.subscribe((confirm: boolean) => {
      this.confirmOpened = false;

      if (this.confirmClosableActions) {
        this.confirmClosableActions(confirm);
      }

      if (!confirm || !this.confirmClosableActions) {
        this.visible = false;
        this.cdr.markForCheck();
      }
    });
  }

  private listenEscape(): void {
    this.documentEscapeListener = this.renderer.listen('document', 'keydown', event => {
      if (event.which !== 27) {
        return;
      }

      if (!this.closeOnEscape) {
        event.preventDefault();
        return;
      }

      const modalElements = document.querySelectorAll('.app-modal-dialog')?.length;
      const dialogZindexes = Array.from(document.querySelectorAll('.p-dialog') ?? [])?.map((element: any) =>
        parseInt(element.style?.zIndex || '', 10)
      );

      const maxZindex = Math.max(...dialogZindexes);

      if (!this.checkZIndex || modalElements === 1 || parseInt(this.dialog?.container?.style?.zIndex || '', 10) === maxZindex) {
        this.showConfirmClosable();
      }
    });
  }
}
