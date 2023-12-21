import { Injectable, OnDestroy } from '@angular/core';
import { ChatMessage } from '@app/shared/commons/chat-messages-container';
import { ComponentStore } from '@ngrx/component-store';
import * as moment from 'moment';
import { EMPTY, Observable, Subscription, firstValueFrom } from 'rxjs';
import { catchError, switchMap, takeUntil, tap } from 'rxjs/operators';
import { ChatService } from './chat.service';
import { Role } from '@app/shared/enums';
import { ChatRespondentDTO } from './chat';
import { RxStompService } from '@stomp/ng2-stompjs';
import { Store, select } from '@ngrx/store';
import { State } from '@app/core';
import { getUserData } from '@app/core/auth/reducers';

export interface ChatState {
  data: Chat[];
  unreadCount: number;
  selectedChat: Chat | null;
  selectedChatMessages: ChatMessage[];
}

export interface Chat {
  id: number;
  respondent: ChatRespondentDTO;
  latestMessage: ChatMessage | null;
}

@Injectable()
export class ChatStore extends ComponentStore<ChatState> implements OnDestroy {
  chatMessages$!: Subscription;
  constructor(private service: ChatService, private rxStompService: RxStompService, private authStore: Store<State>) {
    super({ data: [], unreadCount: 0, selectedChat: null, selectedChatMessages: [] });
  }

  readonly loadCollection = this.effect((effect$: Observable<any>) => {
    return effect$.pipe(
      switchMap(() => {
        return this.service.loadCollection().pipe(
          tap(data => {
            this.setData(data);
          }),
          catchError(() => EMPTY)
        );
      })
    );
  });

  readonly getNotReadCount = this.effect((effect$: Observable<void>) => {
    return effect$.pipe(
      switchMap(() =>
        this.service.getNotReadCount().pipe(
          tap(data => this.setUnread(data)),
          catchError(() => EMPTY)
        )
      )
    );
  });

  readonly setData = this.updater((state, data: Chat[]) => ({
    ...state,
    data,
  }));

  readonly setUnread = this.updater((state, data: number) => ({
    ...state,
    unreadCount: data,
  }));

  readonly setSelectedChat = this.updater((state, data: Chat) => {
    if (state?.selectedChat?.id === data.id) {
      return state;
    }

    if (this.chatMessages$) {
      this.chatMessages$.unsubscribe();
    }

    this.service.getAllMessages(data.id).subscribe(messages => {
      this.setSelectedChatMessage(messages);
      this.watchMessages(data.id);
    });

    return {
      ...state,
      selectedChat: data,
    };
  });

  readonly setSelectedChatMessage = this.updater((state, data: ChatMessage[]) => ({
    ...state,
    selectedChatMessages: data,
  }));

  readonly upsertOne = this.updater((state, data: Chat) => {
    const index = state.data.findIndex((value: Chat) => data.id === value.id);

    if (index < 0) {
      this.setSelectedChat(data);
      return {
        ...state,
        data: [data, ...state.data],
      };
    }

    const storeData = [...state.data];
    storeData[index] = { ...storeData[index], ...data };
    return {
      ...state,
      data: storeData,
    };
  });

  readonly upsertMessage = this.updater((state, data: ChatMessage) => {
    const index = state.selectedChatMessages.findIndex((value: ChatMessage) => data.id === value.id);

    if (index < 0) {
      return {
        ...state,
        selectedChatMessages: [...state.selectedChatMessages, data],
      };
    }

    const storeData = [...state.selectedChatMessages];
    storeData[index] = { ...storeData[index], ...data };
    return {
      ...state,
      selectedChatMessages: storeData,
    };
  });

  readonly data$: Observable<Chat[]> = this.select(state =>
    state.data
      .map(chat => {
        return {
          ...chat,
          respondent: this.service.convertRespondent(chat.respondent),
        };
      })
      .sort((a, b) =>
        a.latestMessage && b.latestMessage ? moment(b.latestMessage?.createdDate).diff(moment(a.latestMessage?.createdDate)) : -1
      )
  );
  readonly unreadCount$: Observable<number> = this.select(state => state.unreadCount);
  readonly selectedChat$: Observable<Chat | null> = this.select(state => {
    if (state.selectedChat) {
      return {
        ...state.selectedChat,
        respondent: this.service.convertRespondent(state.selectedChat.respondent),
      };
    }
    return null;
  });
  readonly selectedChatMessages$: Observable<ChatMessage[]> = this.select(state =>
    state.selectedChatMessages.sort((a, b) => moment(a.createdDate).diff(moment(b.createdDate)))
  );

  readonly getChatByRespondentId = (respondentId: number, role: Role) => {
    return this.get().data.find(item => item.respondent.id === respondentId && item.respondent.role === role);
  };

  readonly getChatByRespondentRole = (role: Role) => {
    return this.get().data.find(item => item.respondent.id === null && item.respondent.role === role);
  };

  readonly getUser = async () => {
    return await firstValueFrom(this.authStore.pipe(select(getUserData), takeUntil(this.destroy$)));
  };

  ngOnDestroy(): void {
    if (this.chatMessages$) {
      this.chatMessages$.unsubscribe();
    }
  }

  private watchMessages(id: number): void {
    this.getUser().then(user => {
      this.chatMessages$ = this.rxStompService.watch(`/refresh-view/chat/${id}/message/${user?.login}`).subscribe(message => {
        const data: ChatMessage = JSON.parse(message.body || '');

        if (!!data && data.id) {
          this.upsertMessage(data);
          if (!data.read) {
            this.service.markAsRead(id).subscribe();
          }
        }
      });
    });
  }
}
