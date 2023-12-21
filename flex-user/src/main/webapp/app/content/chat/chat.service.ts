import { Observable, catchError, of } from 'rxjs';
import { map } from 'rxjs/operators';

import { Injectable } from '@angular/core';
import { DownloadService, HttpService } from '@app/core';

import { ChatRespondentDTO } from './chat';
import { Chat } from './chat.store';
import { FileDTO } from '@app/shared/models';
import { ContentType, Role } from '@app/shared/enums';
import { Dictionary } from '@app/shared/models';
import { ChatMessage } from '@app/shared/commons/chat-messages-container';

@Injectable()
export class ChatService extends HttpService {
  private url = 'api/user/chat';

  getCompanies(): Observable<Partial<Dictionary>[]> {
    return this.get<ChatRespondentDTO[]>(`${this.url}/recipients`).pipe(
      map((response: ChatRespondentDTO[]) =>
        response.map(item => {
          const { ROLE_FLEX_SERVICE_PROVIDER, ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED, ROLE_BALANCING_SERVICE_PROVIDER } = Role;
          item = this.convertRespondent(item);
          let name = '';

          switch (item.role) {
            case ROLE_FLEX_SERVICE_PROVIDER:
              name = `${item.name} (FSP)`;
              break;
            case ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED:
              name = `${item.name} (FSPA)`;
              break;
            case ROLE_BALANCING_SERVICE_PROVIDER:
              name = `${item.name} (BSP)`;
              break;
            default:
              name = item.name;
              break;
          }
          return {
            ...item,
            name,
          };
        })
      )
    );
  }

  downloadAttachment(id: number): void {
    this.get<FileDTO>(`${this.url}/message/${id}/file`).subscribe(({ fileName, base64StringData, fileExtension }: FileDTO) => {
      const [_, contentType = ContentType.TXT] = Object.entries(ContentType).find(([key]) => key === fileExtension) || [];

      DownloadService.saveFileWithParam(base64StringData, fileName, contentType, true);
    });
  }

  saveAttachment(attachment: File, chatId: number): Observable<any> {
    const formData = new FormData();

    formData.append('chatId', chatId.toString());
    formData.append('file', attachment);

    return this.post(`${this.url}/message`, formData, {
      reportProgress: true,
      observe: 'events',
    }).pipe(catchError(() => of(null)));
  }

  sendMessage(content: string | undefined, chatId: number): Observable<any> {
    const formData = new FormData();
    formData.append('chatId', chatId.toString());
    if (content) {
      formData.append('content', content);
    }

    return this.post(`${this.url}/message`, formData);
  }

  startNewChat(respondent: Partial<Dictionary>): Observable<void> {
    return this.post(this.url, { respondent });
  }

  loadCollection(): Observable<Chat[]> {
    return this.get<Chat[]>(this.url);
  }

  getAllMessages(chatId: number): Observable<ChatMessage[]> {
    return this.get<ChatMessage[]>(`${this.url}/${chatId}/messages`);
  }

  getNotReadCount(): Observable<number> {
    return this.get(`${this.url}/unread`);
  }

  markAsRead(chatId: number): Observable<void> {
    return this.put(`${this.url}/${chatId}/mark-as-read`, {});
  }

  convertRespondent(respondent: ChatRespondentDTO): ChatRespondentDTO {
    const { ROLE_ADMIN, ROLE_DISTRIBUTION_SYSTEM_OPERATOR, ROLE_TRANSMISSION_SYSTEM_OPERATOR, ROLE_MARKET_OPERATOR } = Role;

    if (
      [ROLE_ADMIN, ROLE_DISTRIBUTION_SYSTEM_OPERATOR, ROLE_TRANSMISSION_SYSTEM_OPERATOR, ROLE_MARKET_OPERATOR].includes(respondent.role)
    ) {
      respondent.name = `ChatRole.${respondent.role}`;
    }
    return respondent;
  }
}
