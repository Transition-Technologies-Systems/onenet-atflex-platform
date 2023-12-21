import { Component, ElementRef, Injector, Input, OnChanges, OnInit } from '@angular/core';
import { Helpers, ModalService } from '@app/shared/commons';
import { NotificationDTO, NotificationEvent } from '../notification';
import { NotificationsService, NotificationsStore } from '../';

import { ActivatedRoute } from '@angular/router';
import { AlertsStore } from '../alert.store';
import { SessionStorageService } from '@app/core';
import { NotificationsPreviewComponent } from '../preview';
import { ProposalConfirmComponent } from '@app/shared/proposal/confirm';
import { TableExtends } from '@app/shared/services';
import { UserDTO } from '@app/shared/models';
import { ViewConfigurationService } from '@app/shared/commons/view-configuration';

@Component({
  selector: 'app-message-table',
  templateUrl: './message-table.component.html',
  styleUrls: ['./message-table.component.scss'],
})
export class MessageTableComponent extends TableExtends implements OnInit, OnChanges {
  @Input() config: any;

  viewName = undefined;
  disabledHandyScroll = true;
  user: UserDTO | undefined;
  data$ = this.notificationsStore.data$;
  totalRecords$ = this.notificationsStore.totalRecords$;

  constructor(
    injector: Injector,
    route: ActivatedRoute,
    elementRef: ElementRef,
    private alertsStore: AlertsStore,
    sessionStorage: SessionStorageService,
    public modalService: ModalService,
    private notificationsStore: NotificationsStore,
    private notificationService: NotificationsService,
    protected viewConfigurationService: ViewConfigurationService
  ) {
    super(injector, route, elementRef, sessionStorage, viewConfigurationService);
  }

  ngOnInit(): void {}

  ngOnChanges(): void {
    this.page = 0;
    this.getCollection();
  }

  changePage(event: { page: number; size: number }): void {
    this.page = event.page;
    this.rows = event.size;
    this.getCollection();
  }

  getCollection(): void {
    if (this.config.tabIndex === 0) {
      this.notificationsStore.loadCollection({
        page: this.page,
        size: this.rows,
        sort: this.sort,
        filters: Helpers.serializeFilters(this.staticFilters, this.dynamicFilters, ['createdDate']),
      });
      this.data$ = this.notificationsStore.data$;
      this.totalRecords$ = this.notificationsStore.totalRecords$;
    } else if (this.config.tabIndex === 1) {
      this.alertsStore.loadCollection({
        page: this.page,
        size: this.rows,
        sort: this.sort,
        filters: Helpers.serializeFilters(this.staticFilters, this.dynamicFilters, ['createdDate']),
      });
      this.data$ = this.alertsStore.data$;
      this.totalRecords$ = this.alertsStore.totalRecords$;
    }
  }

  getParams(row: NotificationDTO): { [key: string]: string } {
    if (!row.params) {
      return {};
    }

    return Object.entries(row.params).reduce(
      (params: object, [key, data]) => ({ ...params, [key]: typeof data === 'object' ? data.value : data }),
      {}
    );
  }

  markAllAsRead() {
    this.notificationService.markAllAsRead().subscribe(() => this.notificationsStore.newData());
  }

  preview(row: NotificationDTO): void {
    if ([NotificationEvent.SCHEDULING_UNIT_PROPOSAL_TO_BSP, NotificationEvent.SCHEDULING_UNIT_PROPOSAL_TO_FSP].includes(row.eventType)) {
      const idParam = (row.params as any).SCHEDULING_UNIT_PROPOSAL_ID;

      this.modalService.open(ProposalConfirmComponent, {
        baseZIndex: 2000,
        data: {
          id: idParam?.value ?? idParam,
          type: row.eventType === NotificationEvent.SCHEDULING_UNIT_PROPOSAL_TO_BSP ? 'FSP' : 'BSP',
        },
      });

      if (!row?.id || row?.read) {
        return;
      }

      this.notificationService.markAsRead([row.id]).subscribe(() => this.notificationsStore.newData());

      return;
    }

    this.modalService.open(NotificationsPreviewComponent, {
      baseZIndex: 2000,
      data: {
        model: row,
        isAlert: this.config.tabIndex === 1,
      },
    });
  }
}
