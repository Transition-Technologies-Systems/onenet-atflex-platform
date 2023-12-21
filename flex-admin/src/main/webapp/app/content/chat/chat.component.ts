import { AfterViewInit, Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { Subject, takeUntil, tap, forkJoin } from 'rxjs';

import { ModalService } from '@app/shared/commons';
import { RxStompService } from '@stomp/ng2-stompjs';
import { isNil } from 'lodash-es';
import { ChatService } from './chat.service';
import { Chat, ChatStore } from './chat.store';
import { NewChatComponent } from './new-chat/new-chat.component';
import { ChatMessagesContainerComponent, ChatSendMessage } from '@app/shared/commons/chat-messages-container';
import { HttpErrorResponse, HttpEvent, HttpEventType } from '@angular/common/http';
import { AppToastrService } from '@app/core';

@Component({
  selector: 'app-chat',
  templateUrl: './chat.component.html',
  styleUrls: ['./chat.component.scss'],
  providers: [ChatStore],
})
export class ChatComponent implements OnInit, OnDestroy, AfterViewInit {
  @ViewChild(ChatMessagesContainerComponent) chatMessageContainer: ChatMessagesContainerComponent | undefined;

  searchValue: string | null = null;
  selectedChat$ = this.store.selectedChat$;
  selectedChatMessages$ = this.store.selectedChatMessages$;

  data$ = this.store.data$;

  private destroy$ = new Subject<void>();

  constructor(
    private service: ChatService,
    private store: ChatStore,
    private rxStompService: RxStompService,
    private modalService: ModalService,
    private toastr: AppToastrService
  ) {}

  ngOnInit(): void {
    this.getCollection();
  }

  ngAfterViewInit(): void {
    this.watchRxStomp();
    this.subscribeNewMessages();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  getCollection(): void {
    this.store.loadCollection({});
  }

  onSendMessage(event: ChatSendMessage, chatId: number): void {
    this.service.sendMessage(event.content, chatId).subscribe((data: any) => {
      if (!event.attachments.length) {
        return;
      }

      const attachemntsToSave = event.attachments.map((attachment: File, fileIndex: number) =>
        this.service.saveAttachment(attachment, chatId).pipe(
          tap((event: HttpEvent<any>) => {
            if (!event) {
              return;
            }

            switch (event.type) {
              case HttpEventType.UploadProgress:
                if (event.total) {
                  const progress = Math.round((event.loaded / event.total) * 100);

                  this.chatMessageContainer?.setFileProgress(fileIndex, progress);
                } else {
                  this.chatMessageContainer?.setFileProgress(fileIndex, 100);
                }

                break;
              case HttpEventType.Response:
                break;
            }
          })
        )
      );

      forkJoin(attachemntsToSave).subscribe(() => {
        this.chatMessageContainer?.setCompleteSend();
      });
    });
  }

  onAtchmentDownload(id: number): void {
    this.service.downloadAttachment(id);
  }

  selectChat(chat: Chat) {
    this.store.setSelectedChat(chat);
    this.chatMessageContainer?.clearText();
  }

  openNewChatModal(): void {
    this.modalService.open(NewChatComponent).onClose.subscribe(respondent => {
      const { id, role } = respondent;
      if (isNil(id)) {
        return;
      }
      let chat = this.store.getChatByRespondentId(id, role);
      if (isNil(chat)) {
        this.service.startNewChat(respondent).subscribe({
          next: () => {},
          error: (error: HttpErrorResponse) => {
            if (error.error.errorKey === 'error.chat.alreadyExists') {
              chat = this.store.getChatByRespondentRole(role);
              if (chat !== undefined) this.selectChat(chat);
            }
          },
        });
      } else {
        this.toastr.warning('error.chat.alreadyExists');
        this.selectChat(chat);
      }
    });
  }

  private watchRxStomp(): void {
    this.store.getUser().then(user => {
      this.rxStompService
        .watch(`/refresh-view/chat/${user?.login}`)
        .pipe(takeUntil(this.destroy$))
        .subscribe(chat => {
          const data: Chat = JSON.parse(chat.body || '');

          if (!!data && data.id) {
            this.store.upsertOne(data);
          }
        });
    });
  }

  private subscribeNewMessages(): void {
    this.selectedChatMessages$.pipe(takeUntil(this.destroy$)).subscribe(() => {
      if (this.chatMessageContainer) {
        this.chatMessageContainer.scrollEnd();
      }
    });
  }
}
