import { ContentType, FspUserRegistrationCommentDTO, FspUserRegistrationCommentStatus } from '@app/shared/enums';
import { DownloadService, HttpService } from '@app/core';
import { FileDTO, FspUserRegistrationDTO, FspUserRegistrationFileDTO } from '@app/shared/models';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

import { AppService } from '@app/app.service';
import { ChatMessage } from '@app/shared/commons/chat-messages-container/chat-message';
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';

@Injectable()
export class RegistrationThreadService extends HttpService {
  protected url = 'api/fsp-user-registration';

  constructor(httpClient: HttpClient, private translate: TranslateService) {
    super(httpClient);
  }

  downloadAttachment(id: number): void {
    this.get<FileDTO>(`${this.url}/file/${id}`).subscribe(({ fileName, base64StringData, fileExtension }: FileDTO) => {
      const [ext, contentType = ContentType.TXT] = Object.entries(ContentType).find(([key]) => key === fileExtension) || [];

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
                content = this.translate.instant('registrationThread.messages.init', fspUserRegistration);
                break;
              case FspUserRegistrationCommentStatus.GENERATED:
                content = this.translate.instant(`registrationThread.messages.${comment.text}`, comment);
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

  getFspRegistration(): Observable<FspUserRegistrationDTO> {
    return this.get('api/fsp-user-registration/user/fsp');
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
    return this.get(`${this.url}/user/${fspUserRegistrationId}/fsp/withdraw`);
  }
}
