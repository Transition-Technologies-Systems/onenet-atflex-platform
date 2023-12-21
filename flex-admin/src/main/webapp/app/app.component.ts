import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { FilterMatchMode, PrimeNGConfig } from 'primeng/api';
import { LanguageService, LoadingService, SessionStorageService } from './core';
import { NavigationEnd, Router } from '@angular/router';
import { Subject, Subscription } from 'rxjs';

import { TitleService } from './core/title/title.service';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
})
export class AppComponent implements OnInit, OnDestroy {
  loading = true;

  private destroy$ = new Subject<void>();
  private subscription = new Subscription();

  constructor(
    private router: Router,
    private cdRef: ChangeDetectorRef,
    private titleService: TitleService,
    private primengConfig: PrimeNGConfig,
    private loadingService: LoadingService,
    private languageService: LanguageService,
    private sessionStorageService: SessionStorageService
  ) {}

  ngOnInit(): void {
    this.primengConfig.ripple = true;

    this.primengConfig.filterMatchModeOptions = {
      text: [FilterMatchMode.CONTAINS, FilterMatchMode.NOT_CONTAINS, FilterMatchMode.EQUALS, FilterMatchMode.NOT_EQUALS],
      numeric: [
        FilterMatchMode.EQUALS,
        FilterMatchMode.NOT_EQUALS,
        FilterMatchMode.LESS_THAN,
        FilterMatchMode.LESS_THAN_OR_EQUAL_TO,
        FilterMatchMode.GREATER_THAN,
        FilterMatchMode.GREATER_THAN_OR_EQUAL_TO,
      ],
      date: [
        FilterMatchMode.EQUALS,
        FilterMatchMode.LESS_THAN,
        FilterMatchMode.LESS_THAN_OR_EQUAL_TO,
        FilterMatchMode.GREATER_THAN,
        FilterMatchMode.GREATER_THAN_OR_EQUAL_TO,
      ],
    };

    const isIe = window.navigator.userAgent.toLowerCase().indexOf('trident') > -1;

    if (isIe) {
      document.body.classList.add('ie-browser');
    }

    this.subscription.add(this.languageService.init());
    this.subscription.add(this.titleService.changeApplicationTitle$.subscribe());

    this.subscribeChangeRoute();
    this.subscribeLoadingStatus();
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();

    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Subscribe route change
   */
  private subscribeChangeRoute(): void {
    this.router.events.pipe(filter(event => event instanceof NavigationEnd)).subscribe(() => {
      const url = location.href.replace(location.origin, '').replace('/#', '');

      if (!['login', 'registration', 'account/activate'].some((key: string) => url.includes(key))) {
        this.sessionStorageService.setItem('PREV_URL', url);
      }

      this.loadingService.changeRouter();
    });
  }

  /**
   * Subscribe change loading status
   */
  private subscribeLoadingStatus(): void {
    this.subscription.add(
      this.loadingService.loading.subscribe(loading => {
        setTimeout(() => {
          this.loading = loading;
          this.cdRef.detectChanges();
        });
      })
    );
  }
}
