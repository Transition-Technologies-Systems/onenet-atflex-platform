import { ContentType, FspUserRegistrationCommentDTO, FspUserRegistrationCommentStatus, FspUserRegistrationStatus } from '@app/shared/enums';
import { DownloadService, HttpService } from '@app/core';
import { FileDTO, Pageable } from '@app/shared/models';
import { FspUserRegistrationDTO, FspUserRegistrationFileDTO, Tab } from './fsp-registration';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

import { AppService } from '@app/app.service';
import { ChatMessage } from '@app/shared/commons/chat-messages-container/chat-message';
import { FspRegistrationParameters } from './fsp-registration.store';
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';

@Injectable()
export class FspRegistrationService extends HttpService {
  protected url = 'flex-server/api/fsp-user-registration';

  constructor(httpClient: HttpClient, private translate: TranslateService) {
    super(httpClient);
  }

  accept(fspUserRegistrationId: number): Observable<void> {
    return this.get(`${this.url}/admin/${fspUserRegistrationId}/mo/accept`);
  }

  getActiveStatuses(): FspUserRegistrationStatus[] {
    return [
      FspUserRegistrationStatus.NEW,
      FspUserRegistrationStatus.USER_ACCOUNT_ACTIVATED_BY_FSP,
      FspUserRegistrationStatus.CONFIRMED_BY_FSP,
      FspUserRegistrationStatus.PRE_CONFIRMED_BY_MO,
    ];
  }

  downloadAttachment(id: number): void {
    this.get<FileDTO>(`${this.url}/file/${id}`).subscribe(({ fileName, base64StringData, fileExtension }: FileDTO) => {
      const [, contentType = ContentType.TXT] = Object.entries(ContentType).find(([key]) => key === fileExtension) || [];

      DownloadService.saveFileWithParam(base64StringData, fileName, contentType, true);
    });
  }

  getComments(id: number, fspUserRegistration: FspUserRegistrationDTO): Observable<ChatMessage[]> {
    return this.get<FspUserRegistrationCommentDTO[]>(`${this.url}/${id}/comments`).pipe(
      map((response: FspUserRegistrationCommentDTO[]) =>
        response.map((comment: FspUserRegistrationCommentDTO) => {
          const isAutomat = [FspUserRegistrationCommentStatus.INITIAL, FspUserRegistrationCommentStatus.GENERATED].includes(
            comment.creationSource
          );
          let content = comment.text;

          if (isAutomat) {
            switch (comment.creationSource) {
              case FspUserRegistrationCommentStatus.INITIAL:
                content = this.translate.instant('fspRegistration.preview.messages.init', fspUserRegistration);
                break;
              case FspUserRegistrationCommentStatus.GENERATED:
                content = this.translate.instant(`fspRegistration.preview.messages.${comment.text}`, comment);
                break;
            }
          }

          return {
            content,
            id: comment.id,
            automat: isAutomat,
            userId: comment.userId,
            createdBy: comment.createdBy,
            createdDate: comment.createdDate,
            attachments: comment.files.map(({ value, id: commentId }) => ({ name: value, id: commentId })),
          };
        })
      )
    );
  }

  getFspRegistration(fspUserRegistrationId: number): Observable<FspUserRegistrationDTO> {
    return this.get(`${this.url}/admin/${fspUserRegistrationId}`);
  }

  getTabs(): Tab[] {
    return [
      {
        label: this.translate.instant('fspRegistration.tabs.active'),
        type: 'active',
      },
      {
        label: this.translate.instant('fspRegistration.tabs.inactive'),
        type: 'inactive',
      },
    ];
  }

  loadCollection(parameters: FspRegistrationParameters): Observable<Pageable<FspUserRegistrationDTO>> {
    const { tabType, ...params } = parameters;

    const activeStatuses = this.getActiveStatuses();
    const inactiveStatues = Object.keys(FspUserRegistrationStatus).filter(
      (key: string) => !activeStatuses.includes(key as FspUserRegistrationStatus)
    );

    return this.getCollection<FspUserRegistrationDTO>(`${this.url}/admin`, {
      params: {
        ...params,
        'status.in': tabType === 'inactive' ? inactiveStatues : activeStatuses,
      },
    });
  }

  markRead(fspUserRegistrationId: number): Observable<void> {
    return this.get(`${this.url}/admin/${fspUserRegistrationId}/mo/mark-as-read`);
  }

  preConfirm(fspUserRegistrationId: number): Observable<void> {
    return this.get(`${this.url}/admin/${fspUserRegistrationId}/mo/pre-confirm`);
  }

  reject(fspUserRegistrationId: number): Observable<void> {
    return this.get(`${this.url}/admin/${fspUserRegistrationId}/mo/reject`);
  }

  saveAttachment(attachment: File, fspUserRegistrationFile: FspUserRegistrationFileDTO): Observable<any> {
    const formData = new FormData();

    formData.append(
      'fspUserRegFileDTO',
      new Blob([JSON.stringify(fspUserRegistrationFile)], {
        type: 'application/json',
      })
    );

    formData.append('file', attachment);

    return this.post(`${this.url}/file`, formData, {
      reportProgress: true,
      observe: 'events',
    }).pipe(catchError(() => of(null)));
  }

  saveComment(message: string | undefined, fspUserRegistrationId: number): Observable<FspUserRegistrationCommentDTO> {
    return this.post(`${this.url}/comments`, { text: message, fspUserRegistrationId, userId: AppService.userId });
  }

  withdraw(fspUserRegistrationId: number): Observable<void> {
    return this.get(`${this.url}/admin/${fspUserRegistrationId}/fsp/withdraw`);
  }
}
