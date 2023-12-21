import { ChangeDetectionStrategy, Component, OnInit, ViewChild } from '@angular/core';

import { MessageTableConfigDTO } from './message-table/message-table';
import { MessageTableComponent } from './message-table/message-table.component';

@Component({
  selector: 'app-notifications',
  templateUrl: './notifications.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NotificationsComponent implements OnInit {
  @ViewChild(MessageTableComponent, { static: false }) childComp!: MessageTableComponent;

  tableConfig!: MessageTableConfigDTO;
  viewName = 'notifications';
  tabIndex = 0;

  constructor() {}

  ngOnInit(): void {
    this.tableConfig = {
      tabIndex: 0,
      headers: ['createdDate', 'eventType'],
      dateFilter: 'createdDate',
      translatePrefix: 'NotificationEventShort',
      headersPrefix: 'notifications',
      canMarkAsRead: true,
    };
  }

  onChange(): void {
    if (this.tabIndex === 0) {
      this.tableConfig = {
        tabIndex: 0,
        headers: ['createdDate', 'eventType'],
        dateFilter: 'createdDate',
        translatePrefix: 'NotificationEventShort',
        headersPrefix: 'notifications',
        canMarkAsRead: true,
      };
    }
    if (this.tabIndex === 1) {
      this.tableConfig = {
        tabIndex: 1,
        headers: ['createdDate', 'event'],
        dateFilter: 'createdDate',
        translatePrefix: 'AlertEventShort',
        headersPrefix: 'notifications',
        canMarkAsRead: false,
      };
    }
  }
}
