import { ActivationEnd, Router } from '@angular/router';
import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  ElementRef,
  HostListener,
  OnDestroy,
  OnInit,
  Renderer2,
  ViewChild,
} from '@angular/core';
import { Sidebar, SidebarChildren } from './sidebar';

import { Role } from '@app/shared/enums';
import { ScrollPanel } from 'primeng/scrollpanel';
import { SidebarService } from './sidebar.service';
import { filter } from 'rxjs/operators';
import { ChatStore } from '@app/content/chat/chat.store';
import { Observable, takeUntil, Subject } from 'rxjs';
import { RxStompService } from '@stomp/ng2-stompjs';
import { ChatService } from '@app/content/chat/chat.service';

/**
 * Sidebar component
 */
@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html',
  styleUrls: ['sidebar.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [ChatStore, ChatService],
})
export class SidebarComponent implements OnInit, OnDestroy {
  @ViewChild('wrapper', { static: true }) wrapper: ElementRef<HTMLElement> | undefined;
  @ViewChild(ScrollPanel) scrollPanel: ScrollPanel | undefined;

  $unreadChatMessages: Observable<number> = this.chatStore.unreadCount$;

  items: Sidebar[] = [];

  private wrapperHover = false;

  private destroy$ = new Subject<void>();

  constructor(
    private router: Router,
    private renderer: Renderer2,
    private cdr: ChangeDetectorRef,
    private service: SidebarService,
    private chatStore: ChatStore,
    private rxStompService: RxStompService
  ) {}

  ngOnInit(): void {
    this.items = this.service.sidebar;

    this.preparedActiveStatus();

    this.router.events.pipe(filter(event => event instanceof ActivationEnd)).subscribe(() => {
      this.preparedActiveStatus();
      this.cdr.markForCheck();
    });
    this.chatStore.getNotReadCount();
  }

  ngAfterViewInit(): void {
    this.watchRxStomp();
  }

  getItemRole(role: Role[] | undefined): string {
    return role ? role.join(',') : '';
  }

  @HostListener('mouseenter')
  mouseenter(): void {
    if (!this.wrapperHover) {
      this.wrapperHover = true;
      this.renderer.addClass(this.wrapper?.nativeElement, 'active');

      setTimeout(() => {
        const scrollPanel = this.wrapper?.nativeElement?.querySelector('.p-scrollpanel');

        this.renderer.addClass(scrollPanel, 'active');
        this.scrollPanel?.moveBar();
      }, 300);
    }
  }

  @HostListener('mouseleave')
  mouseleave(): void {
    if (this.wrapperHover) {
      this.wrapperHover = false;
      this.renderer.removeClass(this.wrapper?.nativeElement, 'active');

      this.closeAllSidebar();

      const scrollPanel = this.wrapper?.nativeElement?.querySelector('.p-scrollpanel');

      this.renderer.removeClass(scrollPanel, 'active');
      this.scrollPanel?.moveBar();
    }
  }

  /**
   * Set opened flag
   *
   * @param _ The sidebar item
   * @param index The current index
   */
  openChildren(_: any, index: number): void {
    this.items[index].opened = !this.items[index].opened;
  }

  /**
   * Track function by index
   *
   * @param index The current index
   */
  trackByItem(index: number): number {
    return index;
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private closeAllSidebar(): void {
    this.items.forEach((sidebar: Sidebar) => (sidebar.opened = false));
  }

  private isActiveSidebar(item: Sidebar): void {
    if (Array.isArray(item.children)) {
      item.children.forEach((children: SidebarChildren) => {
        children.active = this.router.isActive(children.routerLink, {
          paths: 'subset',
          fragment: 'ignored',
          matrixParams: 'ignored',
          queryParams: 'subset',
        });
      });

      item.active = item.children.some((children: SidebarChildren) => children.active);
      item.opened = item.active;
    } else if (item.routerLink) {
      item.active =
        this.router.isActive(item.routerLink, {
          paths: 'subset',
          fragment: 'ignored',
          matrixParams: 'ignored',
          queryParams: 'subset',
        }) &&
        (!item.notInclude || !this.router.url.includes(item.notInclude));
    }
  }

  private preparedActiveStatus(): void {
    this.items.forEach((sidebar: Sidebar) => this.isActiveSidebar(sidebar));
  }

  private watchRxStomp(): void {
    this.chatStore.getUser().then(user => {
      this.rxStompService
        .watch(`/refresh-view/chat/unread/${user?.login}`)
        .pipe(takeUntil(this.destroy$))
        .subscribe(message => {
          const data = JSON.parse(message.body || '');
          this.chatStore.setUnread(data);
        });
    });
  }
}
