import * as numeral from 'numeral';

import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'numeral',
})
export class NumeralPipe implements PipeTransform {
  transform(
    value: number,
    format: string = '0,0.00',
    decimalSeperator: string = '.',
    showDecimalSeperator: string = ',',
    decimalCharacters: number | null = null
  ): string {
    if (!value && value !== 0) {
      return '';
    }

    const formatedNumber = numeral(value).format(format);
    const [integer, decimal] = formatedNumber.split(decimalSeperator);
    let decimals = decimal ? decimal.split('') : [];
    let otherValue = false;

    decimals = decimals.reverse().filter((d: string) => {
      otherValue = parseInt(d, 10) !== 0 || otherValue;
      return otherValue;
    });

    const integerValue = integer.split(',').join(' ');
    const decimalValue = decimals.reverse().join('');
    const showValue = decimals.length > 0 ? `${integerValue}${showDecimalSeperator}${decimalValue}` : `${integerValue}`;

    if (!decimalCharacters) {
      return showValue;
    } else if (decimalCharacters) {
      const [, fixedDecimals] = parseFloat(decimals.length ? `0.${decimalValue}` : '0')
        .toFixed(2)
        .split('.');

      return `${integerValue}${showDecimalSeperator}${fixedDecimals}`;
    }

    return showValue;
  }
}
