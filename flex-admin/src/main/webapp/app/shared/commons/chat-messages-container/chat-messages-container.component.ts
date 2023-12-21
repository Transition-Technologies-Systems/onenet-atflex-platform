import {
  AfterViewInit,
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  ElementRef,
  EventEmitter,
  Input,
  NgZone,
  OnChanges,
  OnDestroy,
  OnInit,
  Output,
  SimpleChanges,
  TemplateRef,
  ViewChild,
} from '@angular/core';
import { AppToastrService, State, ToastrMessage } from '@app/core';
import { ChatMessage, ChatSendMessage } from './chat-message';
import { Observable, Subject, of } from 'rxjs';
import { Store, select } from '@ngrx/store';

import { ChatAttachmentConfig } from './chat-attachment-config';
import { ChatMessageContainerService } from './chat-messages-container.service';
import { DomSanitizer } from '@angular/platform-browser';
import { Helpers } from '../helpers';
import { UserDTO } from '@app/shared/models';
import { getUserData } from '@app/core/auth/reducers';
import { takeUntil } from 'rxjs/operators';

interface ProgressFileObject {
  [fileKey: string]: number;
}

@Component({
  selector: 'app-chat-messages-container',
  templateUrl: './chat-messages-container.component.html',
  styleUrls: ['./chat-messages-container.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [ChatMessageContainerService],
})
export class ChatMessagesContainerComponent implements OnInit, OnDestroy, OnChanges, AfterViewInit {
  @Input() messages: Observable<ChatMessage[]> = of([]);
  @Input() messagesHeader: TemplateRef<any> | undefined;
  @Input() receiverNotId: number | undefined;
  @Input() receiverId: number | undefined;
  @Input() sendPermission = '';
  @Input() disabled = false;
  @Input() isChatMessage = false;

  @Input() attachmentConfig: Partial<ChatAttachmentConfig> = {
    multiple: true,
    maxFileSize: 10 * 1048576,
    accept: '.doc, .docx, .pdf, .txt, .xls, .xlsx',
  };

  @ViewChild('fileInput') fileInput: ElementRef | undefined;
  @ViewChild('scrollContainer') scrollContainer: ElementRef | undefined;

  @Output() attchmentDownload = new EventEmitter<number>();
  @Output() sendMessage = new EventEmitter<ChatSendMessage>();

  attachments: File[] = [];
  message: string | undefined;
  user: UserDTO | undefined;
  sendIncomplete = false;
  progress = 0;

  private resizeObserver = new ResizeObserver(() => Promise.resolve().then(() => this.scrollEnd()));

  private progressFileObject: ProgressFileObject = {};
  private destroy$ = new Subject<void>();

  constructor(
    public ngZone: NgZone,
    private store: Store<State>,
    public cdr: ChangeDetectorRef,
    private sanitizer: DomSanitizer,
    private toastr: AppToastrService,
    private service: ChatMessageContainerService
  ) {}

  ngOnInit(): void {
    this.subscribeUser();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();

    this.resizeObserver.disconnect();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.messages) {
      this.scrollEnd();
    }
  }

  ngAfterViewInit(): void {
    if (!this.scrollContainer) {
      return;
    }

    this.resizeObserver.observe(this.scrollContainer.nativeElement);
  }

  addAtachments(): void {
    if (this.disabled) {
      return;
    }

    this.fileInput?.nativeElement.click();
  }

  downloadAttachment(id: number): void {
    this.attchmentDownload.next(id);
  }

  isReceiver(message: ChatMessage): boolean {
    if (this.isChatMessage) {
      return !message.myCompanyMessage;
    }

    if (this.receiverId) {
      return message.userId === this.receiverId;
    }

    return message.userId !== this.receiverNotId;
  }

  onFileSelect(event: any): void {
    const files = event.dataTransfer ? event.dataTransfer.files : event.target.files;

    for (const file of files) {
      if (this.validate(file)) {
        if (this.service.isImage(file)) {
          file.objectURL = this.sanitizer.bypassSecurityTrustUrl(window.URL.createObjectURL(file));
        }

        this.attachments.push(file);
      }
    }

    if (this.fileInput) {
      this.fileInput.nativeElement.value = '';
    }
  }

  removeAttachment(index: number): void {
    this.attachments.splice(index, 1);
  }

  setCompleteSend(): void {
    setTimeout(() => {
      this.sendIncomplete = false;
      this.message = undefined;
      this.attachments = [];
      this.progress = 0;
      this.cdr.detectChanges();
    }, 50);
  }

  send(event?: Event): void {
    if (event) {
      event.preventDefault();
    }

    if (this.disabled) {
      return;
    }

    this.progressFileObject = {};

    if ((!this.message || !this.message.length) && !this.attachments.length) {
      this.toastr.warning('warning.chatNoMessage');
      return;
    }

    this.progressFileObject = this.attachments.reduce(
      (previous: ProgressFileObject, file: File, fileIndex: number) => ({ ...previous, [fileIndex]: 0 }),
      {}
    );

    this.sendMessage.emit({ content: this.message, attachments: this.attachments });
    this.sendIncomplete = true;
    this.progress = 5;

    if (!this.attachments.length) {
      this.setCompleteSend();
    }
  }

  setFileProgress(fileIndex: number, progress: number): void {
    this.progressFileObject[fileIndex] = progress;

    const length = Object.keys(this.progressFileObject).length;
    const weight = 95 / length;

    this.progress = Object.values(this.progressFileObject).reduce(
      (value: number, progress: number) => value + (progress * weight) / 100,
      5
    );

    this.cdr.markForCheck();
  }

  scrollEnd(): void {
    Helpers.runOnStableZone(this.ngZone, () =>
      setTimeout(() => {
        if (!this.scrollContainer) {
          return;
        }

        const scrollTop = this.scrollContainer.nativeElement.scrollTop;
        const scrollHeight = this.scrollContainer.nativeElement.scrollHeight;
        const height = this.scrollContainer.nativeElement.clientHeight;

        if (scrollHeight - height > scrollTop) {
          this.scrollContainer.nativeElement.scrollTop = scrollHeight - height;
        }
      })
    );
  }

  clearText(): void {
    Helpers.runOnStableZone(this.ngZone, () => {
      setTimeout(() => {
        this.message = '';
      });
    });
  }

  private subscribeUser(): void {
    this.store.pipe(select(getUserData), takeUntil(this.destroy$)).subscribe((user: UserDTO | undefined) => {
      this.user = user;
    });
  }

  private validate(file: File): boolean {
    if (this.attachmentConfig.accept && !this.service.isFileTypeValid(this.attachmentConfig.accept, file)) {
      this.toastr.warning(new ToastrMessage({ msg: 'warning.fileTypeInvalid', params: { name: file.name } }));
      return false;
    }

    if (this.attachmentConfig.maxFileSize && file.size > this.attachmentConfig.maxFileSize) {
      this.toastr.warning(new ToastrMessage({ msg: 'warning.fileMaxSize', params: { name: file.name } }));
      return false;
    }

    return true;
  }
}
