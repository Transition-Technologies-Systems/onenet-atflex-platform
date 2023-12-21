import { Pipe, PipeTransform } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';

@Pipe({
  name: 'filter',
  pure: false,
})
export class FilterPipe implements PipeTransform {
  constructor(private translate: TranslateService) {}
  transform(items: any[] | null, filter: string | null): any {
    if (!items || !filter) {
      return items;
    }

    return items.filter(({ respondent }) => this.translate.instant(respondent.name).toLowerCase().includes(filter.toLowerCase()));
  }
}
