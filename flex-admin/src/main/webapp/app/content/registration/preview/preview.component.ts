import { AppToastrService, ToastrMessage } from '@app/core';
import { ChangeDetectorRef, Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { ChatMessage, ChatMessagesContainerComponent, ChatSendMessage } from '@app/shared/commons/chat-messages-container';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { FspUserRegistrationCommentDTO, FspUserRegistrationStatus, Role } from '@app/shared/enums';
import { HttpEvent, HttpEventType } from '@angular/common/http';
import { Observable, Subject, forkJoin, of, catchError } from 'rxjs';
import { switchMap, takeUntil, tap } from 'rxjs/operators';

import { ConfirmationService } from 'primeng/api';
import { FspRegistrationService } from '../fsp-registration.service';
import { FspRegistrationStore } from '../fsp-registration.store';
import { FspUserRegistrationDTO } from '../fsp-registration';
import { TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-registration-preview',
  templateUrl: './preview.component.html',
  styleUrls: ['./preview.component.scss'],
  providers: [ConfirmationService],
})
export class FspRegistrationPreviewComponent implements OnInit, OnDestroy {
  @ViewChild(ChatMessagesContainerComponent) chatMessageContainer: ChatMessagesContainerComponent | undefined;

  messages$: Observable<ChatMessage[]> = of([]);
  fspUserRegistration: FspUserRegistrationDTO = this.config.data;

  get fullName(): string {
    const data = this.fspUserRegistration;

    return `${data.firstName} ${data.lastName} &bull;	${data.companyName}`;
  }

  get roleName(): string {
    switch (this.fspUserRegistration.userTargetRole) {
      case Role.ROLE_BALANCING_SERVICE_PROVIDER:
        return this.translate.instant('RoleShort.ROLE_BALANCING_SERVICE_PROVIDER');
      case Role.ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED:
        return this.translate.instant('RoleShort.ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED');
    }

    return this.translate.instant('RoleShort.ROLE_FLEX_SERVICE_PROVIDER');
  }

  get isClosed(): boolean {
    return [
      FspUserRegistrationStatus.ACCEPTED_BY_MO,
      FspUserRegistrationStatus.REJECTED_BY_MO,
      FspUserRegistrationStatus.WITHDRAWN_BY_FSP,
    ].includes(this.fspUserRegistration.status);
  }

  get hasRegistrationThread(): boolean {
    return ![FspUserRegistrationStatus.NEW, FspUserRegistrationStatus.CONFIRMED_BY_FSP].includes(this.fspUserRegistration.status);
  }

  get isActiveActions(): boolean {
    return this.service.getActiveStatuses().includes(this.fspUserRegistration.status);
  }

  get isNewStatus(): boolean {
    return [FspUserRegistrationStatus.NEW].includes(this.fspUserRegistration.status);
  }

  get isActivateAccount(): boolean {
    return ![FspUserRegistrationStatus.NEW, FspUserRegistrationStatus.PRE_CONFIRMED_BY_MO].includes(this.fspUserRegistration.status);
  }

  private destroy$ = new Subject<void>();

  constructor(
    public ref: DynamicDialogRef,
    public cdr: ChangeDetectorRef,
    public toastr: AppToastrService,
    public translate: TranslateService,
    public config: DynamicDialogConfig,
    private store: FspRegistrationStore,
    private service: FspRegistrationService,
    private confirmationService: ConfirmationService
  ) {}

  ngOnInit(): void {
    this.getComments();
    this.subsribeRegistrationUser();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  accept(event: Event): void {
    this.confirmationService.confirm({
      target: event.target || undefined,
      message: this.translate.instant('fspRegistration.preview.actions.accept.question', { value: this.roleName }),
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.service
          .accept(this.fspUserRegistration.id)
          .pipe(
            switchMap(() => of(this.store.updateDataInCollection(this.fspUserRegistration.id))),
            catchError((): any => this.toastr.error('fspRegistration.preview.actions.accept.error'))
          )
          .subscribe(() => {
            const successMessage = new ToastrMessage({
              msg: 'fspRegistration.preview.actions.accept.success',
              params: this.fspUserRegistration,
            });
            this.toastr.success(successMessage);
            this.getComments();
          });
      },
    });
  }

  close(): void {
    this.ref.close();
  }

  markRead(event: Event): void {
    this.confirmationService.confirm({
      target: event.target || undefined,
      message: this.translate.instant('fspRegistration.preview.actions.markRead.question'),
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.service
          .markRead(this.fspUserRegistration.id)
          .pipe(
            switchMap(() => of(this.store.updateDataInCollection(this.fspUserRegistration.id))),
            catchError((): any => this.toastr.error('fspRegistration.preview.actions.markRead.error'))
          )
          .subscribe(() => {
            const successMessage = new ToastrMessage({
              msg: 'fspRegistration.preview.actions.markRead.success',
              params: this.fspUserRegistration,
            });

            this.toastr.success(successMessage);
            this.getComments();
          });
      },
    });
  }

  onAtchmentDownload(id: number): void {
    this.service.downloadAttachment(id);
  }

  onSendMessage(event: ChatSendMessage): void {
    this.service.saveComment(event.content, this.fspUserRegistration.id).subscribe((data: FspUserRegistrationCommentDTO) => {
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

  preConfirm(event: Event): void {
    this.confirmationService.confirm({
      target: event.target || undefined,
      message: this.translate.instant('fspRegistration.preview.actions.preConfirm.question'),
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.service
          .preConfirm(this.fspUserRegistration.id)
          .pipe(
            switchMap(() => of(this.store.updateDataInCollection(this.fspUserRegistration.id))),
            catchError((): any => this.toastr.error('fspRegistration.preview.actions.preConfirm.error'))
          )
          .subscribe(() => {
            const successMessage = new ToastrMessage({
              msg: 'fspRegistration.preview.actions.preConfirm.success',
              params: this.fspUserRegistration,
            });

            this.toastr.success(successMessage);
            this.getComments();
          });
      },
    });
  }

  reject(event: Event): void {
    this.confirmationService.confirm({
      target: event.target || undefined,
      message: this.translate.instant('fspRegistration.preview.actions.reject.question', { value: this.roleName }),
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.service
          .reject(this.fspUserRegistration.id)
          .pipe(
            switchMap(() => of(this.store.updateDataInCollection(this.fspUserRegistration.id))),
            catchError((): any => this.toastr.error('fspRegistration.preview.actions.reject.error'))
          )
          .subscribe(() => {
            const successMessage = new ToastrMessage({
              msg: 'fspRegistration.preview.actions.reject.success',
              params: this.fspUserRegistration,
            });

            this.toastr.success(successMessage);
            this.getComments();
          });
      },
    });
  }

  withdraw(event: Event): void {
    this.confirmationService.confirm({
      target: event.target || undefined,
      message: this.translate.instant('fspRegistration.preview.actions.withdraw.question'),
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.service
          .withdraw(this.fspUserRegistration.id)
          .pipe(
            switchMap(() => of(this.store.updateDataInCollection(this.fspUserRegistration.id))),
            catchError((): any => this.toastr.error('fspRegistration.preview.actions.withdraw.error'))
          )
          .subscribe(() => {
            const successMessage = new ToastrMessage({
              msg: 'fspRegistration.preview.actions.withdraw.success',
              params: this.fspUserRegistration,
            });

            this.toastr.success(successMessage);
            this.getComments();
          });
      },
    });
  }

  private getComments(): void {
    this.service.getComments(this.fspUserRegistration.id, this.fspUserRegistration).subscribe((data: ChatMessage[]) => {
      this.messages$ = of(data);
    });
  }

  private subsribeRegistrationUser(): void {
    this.store
      .dataById$(this.fspUserRegistration.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe((data: FspUserRegistrationDTO | undefined) => {
        this.fspUserRegistration = data || this.config.data;
        this.cdr.markForCheck();
      });
  }
}
