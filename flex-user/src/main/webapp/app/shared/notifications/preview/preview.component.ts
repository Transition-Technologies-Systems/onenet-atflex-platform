import * as moment from 'moment';

import { AlertDTO, NotificationDTO, NotificationEvent } from '../notification';
import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';

import { NotificationsService } from '../notifications.service';
import { NotificationsStore } from '../notifications.store';
import { TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-notifications-preview',
  templateUrl: './preview.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NotificationsPreviewComponent implements OnInit {
  model: NotificationDTO | AlertDTO | undefined;
  showEventType = true;

  get isAlert(): boolean {
    return !!this.config.data?.isAlert;
  }

  get alertModel(): AlertDTO {
    return this.model as AlertDTO;
  }

  get notificationModel(): NotificationDTO {
    return this.model as NotificationDTO;
  }

  get params(): { [key: string]: any } {
    return Object.entries(this.notificationModel.params).reduce(
      (params: object, [key, data]: [string, { value: string; object: string | null }]) => {
        const DATE_REGEX = /\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}Z/gm;

        if (data.object) {
          return {
            ...params,
            [key]: JSON.parse(data.object),
          };
        }

        const value = data.value;
        let translatedValue = value;

        if (value && new RegExp(DATE_REGEX).test(value)) {
          translatedValue = moment(value).format('DD/MM/YYYY HH:mm:ss');
        } else if (['true', 'false'].includes(value)) {
          translatedValue = this.translate.instant(`Boolean.${value === 'true'}`);
        } else if (value === null || value === 'null') {
          translatedValue = '';
        } else if (['DER_TYPES', 'SU_TYPES'].includes(value)) {
          translatedValue = this.translate.instant(`DictionaryType.${value}`);
        } else if (['UP', 'DOWN', 'BOTH'].includes(value)) {
          translatedValue = this.translate.instant(`DirectionOfDeviationType.${value}`);
        } else {
          translatedValue = this.translate.instant(value);
        }

        return {
          ...params,
          [key]: translatedValue,
        };
      },
      {}
    );
  }

  get paramsWithArray(): string[] {
    return Object.keys(this.params).filter((key: string) => Array.isArray(this.params[key]) && this.params[key].length);
  }

  constructor(
    public ref: DynamicDialogRef,
    private store: NotificationsStore,
    public config: DynamicDialogConfig,
    private translate: TranslateService,
    private service: NotificationsService
  ) {}

  ngOnInit(): void {
    this.model = this.config.data?.model;

    if (!this.model?.id || this.model?.read) {
      return;
    }

    this.service.markAsRead([this.model.id]).subscribe(() => this.store.newData());
  }

  close(): void {
    this.ref.close();
  }

  getNotification(key?: string): string {
    const translated = this.translate.instant('NotificationEvent.' + (key ?? this.notificationModel.eventType), this.params);

    if (key === NotificationEvent.BID_IMPORT) {
      this.showEventType = Object.keys(this.params).includes('IMPORTED_BIDS');
    }

    const start = translated.indexOf('<ul>');
    const end = translated.indexOf('</ul>');

    if (start === -1) {
      return translated;
    }

    const textForUl = translated.slice(start + 4, end);

    const text = textForUl
      .split('<li>')
      .filter((value: string) => !value.endsWith(': </li>'))
      .join('<li>');

    return [translated.slice(0, start), `<ul>${text}</ul>`, translated.slice(end + 5, translated.length)].join('');
  }
}
