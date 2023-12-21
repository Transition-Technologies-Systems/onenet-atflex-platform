import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'printDictionary',
})
export class PrintDictionaryPipe implements PipeTransform {
  transform(value: any | any[], key: string = 'value', joinValue: string = ', '): string {
    if (!value) {
      return '';
    }

    if (Array.isArray(value)) {
      return value.map((data: any) => data[key]).join(joinValue);
    }

    return value[key];
  }
}
