import { catchError } from 'rxjs/operators';
import { AppToastrService, ToastrMessage } from '@app/core';
import { ChatMessage, ChatMessagesContainerComponent, ChatSendMessage } from '@app/shared/commons/chat-messages-container';
import { Component, OnInit, ViewChild } from '@angular/core';
import { FspUserRegistrationCommentDTO, FspUserRegistrationStatus } from '@app/shared/enums';
import { HttpEvent, HttpEventType } from '@angular/common/http';
import { Observable, forkJoin, of, tap } from 'rxjs';

import { ActivatedRoute } from '@angular/router';
import { ConfirmationService } from 'primeng/api';
import { FspUserRegistrationDTO } from '@app/shared/models';
import { RegistrationThreadService } from './registration-thread.service';
import { TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-registration-thread',
  templateUrl: './registration-thread.component.html',
  styleUrls: ['./registration-thread.component.scss'],
  providers: [ConfirmationService],
})
export class RegistrationThreadComponent implements OnInit {
  @ViewChild(ChatMessagesContainerComponent) chatMessageContainer: ChatMessagesContainerComponent | undefined;

  messages$: Observable<ChatMessage[]> = of([]);
  fspUserRegistration: FspUserRegistrationDTO;

  get fullName(): string {
    const data = this.fspUserRegistration;

    if (!data) {
      return '';
    }

    return `${data.firstName} ${data.lastName} &bull;	${data.companyName}`;
  }

  get isClosed(): boolean {
    return [
      FspUserRegistrationStatus.ACCEPTED_BY_MO,
      FspUserRegistrationStatus.REJECTED_BY_MO,
      FspUserRegistrationStatus.WITHDRAWN_BY_FSP,
    ].includes(this.fspUserRegistration.status);
  }

  constructor(
    private route: ActivatedRoute,
    public toastr: AppToastrService,
    public translate: TranslateService,
    private service: RegistrationThreadService,
    private confirmationService: ConfirmationService
  ) {
    this.fspUserRegistration = this.route.snapshot.data?.fspUserRegistration;
  }

  ngOnInit(): void {
    this.getComments();
  }

  onAtchmentDownload(id: number): void {
    this.service.downloadAttachment(id);
  }

  onSendMessage(event: ChatSendMessage): void {
    this.service.saveComment(event.content, this.fspUserRegistration?.id).subscribe((data: FspUserRegistrationCommentDTO) => {
      if (!event.attachments.length) {
        this.getComments();
        return;
      }

      const attachemntsToSave = event.attachments.map((attachment: File, fileIndex: number) =>
        this.service
          .saveAttachment(attachment, {
            fspUserRegistrationId: this.fspUserRegistration.id,
            fspUserRegistrationCommentId: data.id,
          })
          .pipe(
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
        this.getComments();
        this.chatMessageContainer?.setCompleteSend();
      });
    });
  }

  withdraw(event: Event): void {
    this.confirmationService.confirm({
      target: event.target || undefined,
      message: this.translate.instant('registrationThread.actions.withdraw.question'),
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.service
          .withdraw(this.fspUserRegistration?.id)
          .pipe(catchError((): any => this.toastr.error('fspRegistration.preview.actions.withdraw.error')))
          .subscribe(() => {
            const successMessage = new ToastrMessage({
              msg: 'registrationThread.actions.withdraw.success',
              params: this.fspUserRegistration,
            });

            this.fspUserRegistration.status = FspUserRegistrationStatus.WITHDRAWN_BY_FSP;
            this.toastr.success(successMessage);
            this.getComments();
          });
      },
    });
  }

  private getComments(): void {
    this.service.getComments(this.fspUserRegistration?.id, this.fspUserRegistration).subscribe((data: ChatMessage[]) => {
      this.messages$ = of(data);
    });
  }
}
