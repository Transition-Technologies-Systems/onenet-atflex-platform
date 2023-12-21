import { Pipe, PipeTransform } from '@angular/core';

import { Moment } from 'moment';
import { moment } from 'polyfills';

@Pipe({
  name: 'chatMessageDate',
})
export class ChatMessageDatePipe implements PipeTransform {
  transform(value: string | Moment | Date): string {
    if (!value) {
      return '';
    }

    const now = moment().set({ h: 0, m: 0, s: 0, ms: 0 });
    const date = moment(value);

    if (date.isSameOrAfter(now)) {
      return date.format('HH:mm');
    }

    if (date.year() === now.year()) {
      return date.format('DD/MM HH:mm');
    }

    return date.format('dd/MM/yyyy HH:mm');
  }
}
