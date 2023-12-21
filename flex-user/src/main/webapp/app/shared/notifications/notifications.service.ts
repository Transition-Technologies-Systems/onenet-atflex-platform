import * as AuthActions from '@app/core/auth/actions';
import * as moment from 'moment';

import { AlertDTO, NotificationDTO } from './notification';
import { DefaultParameters, OfferReminder, Pageable, UserDTO } from '@app/shared/models';
import { HttpService, LanguageService, State } from '@app/core';
import { Injectable, OnDestroy } from '@angular/core';
import { Observable, Subject, Subscription, of, timer } from 'rxjs';
import { catchError, map, takeWhile } from 'rxjs/operators';
import {
  obligedToTakePartInBalancingEnergy,
  offerHasBeenSubmittedInBalancingEnergy,
  updateReminder,
} from '@app/core/configuration/actions';

import { HttpClient } from '@angular/common/http';
import { MinimalDTO } from '@app/shared/models/minimal';
import { Router } from '@angular/router';
import { RxStompService } from '@stomp/ng2-stompjs';
import { Store } from '@ngrx/store';
import { TranslateService } from '@ngx-translate/core';

type WatchRxStompStatus = 'PENDING' | 'CLOSED' | 'ACTIVE';

@Injectable()
export class NotificationsService extends HttpService implements OnDestroy {
  protected url = 'api/notifications';
  protected alertUrl = 'api/activity-monitor';

  set user(value: UserDTO | undefined) {
    this.userData = value;

    if (this.watchRxStompStatus === 'PENDING') {
      this.watchRxStomp();
    }
  }

  get user(): UserDTO | undefined {
    return this.userData;
  }

  expired = false;
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
    private translateService: TranslateService
  ) {
    super(httpClient);
  }

  ngOnDestroy(): void {
    this.expired = true;
  }

  initRxStomp(): void {
    this.getOffersReminder();
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

  getOffersReminder(): void {
    this.expired = true;

    this.get<OfferReminder>('api/user/auctions-day-ahead/offers/reminder')
      .pipe(
        catchError(() =>
          of({
            auctionGateClosureTime: null,
            auctionGateOpeningTime: null,
            auctionId: null,
            auctionName: null,
            hasReminder: false,
          })
        )
      )
      .subscribe((response: OfferReminder) => {
        if (response.hasReminder && moment().isBefore(moment(response.auctionGateClosureTime))) {
          this.expired = false;

          const toDate = moment(response.auctionGateClosureTime).toDate().getTime();

          timer(0, 1000)
            .pipe(
              takeWhile(() => !this.expired),
              map(() => {
                const diff = toDate - new Date().getTime();

                if (diff <= 0) {
                  this.expired = true;
                  this.store.dispatch(updateReminder({ data: { ...response, hasReminder: false } }));
                }

                return diff;
              })
            )
            .subscribe();
        } else {
          response.hasReminder = false;
        }

        this.store.dispatch(updateReminder({ data: response }));
      });
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
      } else {
        this.notificationsSubsribe.next(message);
      }
    });

    this.rxStopmSubscription$ = this.rxStompService.watch(`/topic/${this.user.login}/reminder`).subscribe(message => {
      const data = JSON.parse(message.body || '');

      if (data === 'DA_OBLIGED_TO_TAKE_PART_IN_BALANCING_ENERGY') {
        this.getOffersReminder();

        this.store.dispatch(obligedToTakePartInBalancingEnergy({ obligated: true }));
      } else if (data === 'DA_OFFER_HAS_BEEN_SUBMITTED_IN_BALANCING_ENERGY') {
        this.getOffersReminder();

        this.store.dispatch(offerHasBeenSubmittedInBalancingEnergy({ submittedOffer: true }));
      }
    });

    this.rxStompDictionarySubscription$ = this.rxStompService.watch(`/topic/dictionary-update`).subscribe(message => {
      this.languageService.getDynamicTranslate(this.translateService.currentLang).subscribe();
    });
  }
}
