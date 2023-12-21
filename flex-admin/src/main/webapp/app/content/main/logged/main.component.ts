import * as AuthActions from '@app/core/auth/actions';

import { ActivatedRoute, NavigationEnd, Router } from '@angular/router';
import { AuthService, State } from '@app/core';
import { Component, OnDestroy, OnInit, ViewEncapsulation } from '@angular/core';
import { NotificationsService, NotificationsStore } from '@app/shared/notifications';
import { Observable, Subject } from 'rxjs';
import { Store, select } from '@ngrx/store';
import { filter, finalize, map, takeUntil } from 'rxjs/operators';

import { HttpClient } from '@angular/common/http';
import { UserDTO } from '@app/shared/models';
import { environment as env } from '@env/environment';
import { getUserData } from '@app/core/auth/reducers';

@Component({
  selector: 'app-logged-main-page',
  templateUrl: './main.component.html',
  styleUrls: ['./main.component.scss'],
  encapsulation: ViewEncapsulation.None,
})
export class LoggedMainPageComponent implements OnInit, OnDestroy {
  user: UserDTO | undefined;
  unreadNotifications$: Observable<number> = this.notificationsStore.undread$;
  hideSidebar = false;
  containerCustomClass = '';

  get userName(): string {
    if (!this.user) {
      return '';
    }

    return `${this.user?.firstName} ${this.user?.lastName}`;
  }

  private destroy$ = new Subject<void>();

  constructor(
    private router: Router,
    private store: Store<State>,
    private http: HttpClient,
    private authService: AuthService,
    private activatedRoute: ActivatedRoute,
    private notificationsStore: NotificationsStore,
    private notificationsService: NotificationsService
  ) {}

  ngOnInit(): void {
    this.subscribeUser();

    this.authService.hasAuthority('FLEX_ADMIN_VIEW_NOTIFICATION').then((hasAuthority: boolean) => {
      if (hasAuthority) {
        this.subscribeNotifications();
        this.notificationsService.initRxStomp();
      }
    });

    this.router.events
      .pipe(
        filter(event => event instanceof NavigationEnd),
        map(() => this.getRouterData())
      )
      .subscribe(data => this.parseRouterData(data));

    this.parseRouterData(this.getRouterData());
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.notificationsService.destroyRxStomp();
  }

  logout(): void {
    this.http
      .put(`${env.SERVER_API_URL}flex-server/api/users/logout-current`, null)
      .pipe(
        finalize(() => {
          this.store.dispatch(AuthActions.logout());
        })
      )
      .subscribe();
  }

  private getRouterData(): any {
    let child = this.activatedRoute.firstChild;
    while (child) {
      if (child.firstChild) {
        child = child.firstChild;
      } else if (child.snapshot.data) {
        return child.snapshot.data;
      } else {
        return null;
      }
    }
    return null;
  }

  private parseRouterData(data: any): void {
    this.hideSidebar = !!data?.hideSidebar;
    this.containerCustomClass = data?.containerCustomClass || '';
  }

  private subscribeNotifications(): void {
    this.notificationsStore.newData();

    this.notificationsService.notificationsSubsribe.subscribe(() => {
      this.notificationsStore.newData();
    });
  }

  private subscribeUser(): void {
    this.store.pipe(select(getUserData), takeUntil(this.destroy$)).subscribe((user: UserDTO | undefined) => {
      this.notificationsService.user = user;
      this.user = user;
    });
  }
}
