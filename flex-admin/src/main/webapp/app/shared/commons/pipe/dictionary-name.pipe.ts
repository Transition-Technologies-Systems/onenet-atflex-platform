import { Pipe, PipeTransform } from '@angular/core';
import { Dictionary } from '@app/shared/models';

@Pipe({
  name: 'dictionaryName',
})
export class DictionaryNamePipe implements PipeTransform {
  transform(findValue: string | number | undefined, dictionary: Dictionary[] = []): string {
    if (!dictionary || !findValue) {
      return '';
    }

    const data = dictionary.find(({ value }) => value === findValue);

    return data && data.label ? data.label : '';
  }
}
