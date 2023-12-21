import * as AuthActions from '@app/core/auth/actions';

import { AlertDTO, NotificationDTO } from './notification';
import { AppToastrService, HttpService, LanguageService, State, ToastrMessage } from '@app/core';
import { Observable, Subject, Subscription } from 'rxjs';
import { DefaultParameters, Pageable, UserDTO } from '@app/shared/models';

import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { MinimalDTO } from '@app/shared/models/minimal';
import { Router } from '@angular/router';
import { RxStompService } from '@stomp/ng2-stompjs';
import { Store } from '@ngrx/store';
import { TranslateService } from '@ngx-translate/core';
import { map } from 'rxjs/operators';

type WatchRxStompStatus = 'PENDING' | 'CLOSED' | 'ACTIVE';

@Injectable()
export class NotificationsService extends HttpService {
  protected url = 'flex-server/api/notifications';
  protected alertUrl = 'flex-server/api/activity-monitor';

  set user(value: UserDTO | undefined) {
    this.userData = value;

    if (this.watchRxStompStatus === 'PENDING') {
      this.watchRxStomp();
    }
  }

  get user(): UserDTO | undefined {
    return this.userData;
  }

  notificationsSubsribe = new Subject<any>();

  private userData: UserDTO | undefined;
  private watchRxStompStatus: WatchRxStompStatus = 'CLOSED';
  private rxStopmSubscription$ = new Subscription();
  private rxStompDictionarySubscription$ = new Subscription();

  constructor(
    httpClient: HttpClient,
    private router: Router,
    private store: Store<State>,
    private rxStompService: RxStompService,
    private languageService: LanguageService,
    private translateService: TranslateService,
    private toastr: AppToastrService
  ) {
    super(httpClient);
  }

  initRxStomp(): void {
    this.watchRxStomp();
  }

  destroyRxStomp(): void {
    this.watchRxStompStatus = 'CLOSED';
    this.rxStopmSubscription$.unsubscribe();
    this.rxStompDictionarySubscription$.unsubscribe();
  }

  getNotReadCount(): Observable<number> {
    return this.get<MinimalDTO<number, number>>(`${this.url}/count-not-read`).pipe(map(response => response.value));
  }

  markAsRead(ids: number[]): Observable<void> {
    return this.get(`${this.url}/mark-as-read`, {
      params: { ids },
    });
  }

  loadCollection(parameters: DefaultParameters): Observable<Pageable<NotificationDTO>> {
    const { filters, ...params } = parameters;

    return this.getCollection<NotificationDTO>(this.url, {
      params: {
        ...params,
        ...filters,
      },
    });
  }

  loadAlertCollection(parameters: DefaultParameters): Observable<Pageable<AlertDTO>> {
    const { filters, ...params } = parameters;

    return this.getCollection<AlertDTO>(this.alertUrl, {
      params: {
        ...params,
        ...filters,
      },
    });
  }

  markAllAsRead(): Observable<void> {
    return this.get(`${this.url}/mark-all-as-read`);
  }

  private watchRxStomp(): void {
    this.rxStopmSubscription$.unsubscribe();
    this.rxStompDictionarySubscription$.unsubscribe();

    this.watchRxStompStatus = 'PENDING';

    if (!this.user) {
      return;
    }

    this.rxStopmSubscription$ = this.rxStompService.watch(`/topic/${this.user.login}/events`).subscribe(message => {
      const data = JSON.parse(message.body || '');

      if (data?.event === 'LOGOUT_USER') {
        this.store.dispatch(AuthActions.logout());
        this.router.navigate(['/login']);
      } else if (data?.event === 'DISAGGREGATION_COMPLETED' || data?.event === 'DISAGGREGATION_FAILED') {
        const id = data?.notification?.params?.ID?.value;

        const type = data?.event === 'DISAGGREGATION_COMPLETED' ? 'success' : 'error';

        const toastrMessage = new ToastrMessage({
          msg: `auctions.actions.bidsEvaluation.importAGNO.websocket.${type}`,
          params: {
            id,
          },
        });
        this.toastr[type](toastrMessage);
      } else if (data?.event === 'CONNECTION_TO_ALGORITHM_SERVICE_LOST') {
        const id = data?.notification?.params?.ID?.value;
        const toastrMessage = new ToastrMessage({
          msg: `auctions.actions.bidsEvaluation.importAGNO.websocket.${data?.event}`,
          params: {
            id,
          },
        });
        this.toastr.error(toastrMessage);
      } else {
        this.notificationsSubsribe.next(message);
      }
    });

    this.rxStompDictionarySubscription$ = this.rxStompService.watch(`/topic/dictionary-update`).subscribe(message => {
      this.languageService.getDynamicTranslate(this.translateService.currentLang).subscribe();
    });
  }
}
