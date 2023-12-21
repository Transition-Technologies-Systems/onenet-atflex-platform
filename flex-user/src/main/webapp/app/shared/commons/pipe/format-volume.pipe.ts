import { Pipe, PipeTransform } from '@angular/core';
import { AuctionDayAheadType } from '@app/content/auctions/enums';

@Pipe({
  name: 'showVolume',
})
export class FormatVolumePipe implements PipeTransform {
  transform(row: any, key: string, suffix: string | null = null): string {
    let value = row[key];
    const unit = suffix ?? this.getUnit('volume', row);
    if (value.includes('/')) {
      value = value.replace('/', ' ' + unit + '/');
    }
    return `${value} ${unit}`;
  }

  private getUnit(type: 'price' | 'volume', row: any): string {
    switch (row.type) {
      case AuctionDayAheadType.CAPACITY:
        return type === 'price' ? 'PLN/kW' : 'kW';
      case AuctionDayAheadType.ENERGY:
        return type === 'price' ? 'PLN/kWh' : 'kWh';
      default:
        return '';
    }
  }
}
