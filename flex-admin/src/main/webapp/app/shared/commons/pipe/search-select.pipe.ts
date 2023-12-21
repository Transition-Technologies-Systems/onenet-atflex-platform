import { Pipe, PipeTransform } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';

@Pipe({
  name: 'searchSelect',
})
export class SearchSelectPipe implements PipeTransform {
  constructor(private translate: TranslateService) {}

  transform(value: any[], search: string, optionLabel: string): any[] {
    if (search.trim().length === 0 || search.trim() === '') {
      return value;
    }

    return value?.filter(item => this.translate.instant(item[optionLabel]).toLowerCase().includes(search.toLowerCase()));
  }
}
