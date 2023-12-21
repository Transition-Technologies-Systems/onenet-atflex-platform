import * as moment from 'moment';

import { AuctionDayAheadDTO, AuctionsSeriesDTO, Tab, TabType } from './day-ahead';
import { AuctionDayAheadType, AuctionStatus } from '../enums';
import { Dictionary, Pageable, ProductDTO } from '@app/shared/models';
import { Observable, map } from 'rxjs';

import { DayAheadParameters } from './day-ahead.store';
import { Helpers } from '@app/shared/commons';
import { HttpClient } from '@angular/common/http';
import { HttpService } from '@app/core';
import { Injectable } from '@angular/core';
import { ProductDirection } from '@app/shared/enums';
import { TranslateService } from '@ngx-translate/core';

@Injectable()
export class DayAheadService extends HttpService {
  protected url = 'api/user/auctions-day-ahead';
  protected seriesUrl = 'api/user/auctions-series';

  constructor(httpClient: HttpClient, private translate: TranslateService) {
    super(httpClient);
  }

  formatAvailability(dateFrom: string, dateTo: string): string {
    const dates = [dateFrom ? moment(dateFrom).format('HH:mm') : null, dateTo ? moment(dateTo).format('HH:mm') : null];

    if (dateFrom && dateTo) {
      if (moment(dateTo).isAfter(moment(dateFrom).endOf('day'))) {
        dates[1] = '24:00';
      }
    }

    return dates.filter(Boolean).join(' - ') || '';
  }

  getAuction(id: number): Observable<AuctionDayAheadDTO> {
    return this.get(`${this.url}/${id}`);
  }

  getAuctionProducts(type: AuctionDayAheadType | null): Observable<Array<Dictionary>> {
    return this.get<{ id: number; name: string }[]>(`${this.url}/get-products-used-in-auction`, {
      params: { type },
    }).pipe(map(response => response.map(({ id, name }) => ({ id, value: id, label: name }))));
  }

  getSeries(id: number): Observable<AuctionsSeriesDTO> {
    return this.get(`${this.seriesUrl}/${id}`);
  }

  getOffers(parameters: any): Observable<any[]> {
    const { id, ...params } = parameters;

    return this.get('api/user/auctions-day-ahead/offers', {
      params: {
        ...params,
        'auctionDayAhead.id': id,
      },
    });
  }

  getProduct(id: number): Observable<ProductDTO> {
    return this.get(`api/user/products/${id}`);
  }

  getProducts(productDirection: ProductDirection[]): Observable<Array<Dictionary & { minBidSize: number; maxBidSize: number }>> {
    return this.get<{ id: number; shortName: string; minBidSize: number; maxBidSize: number }[]>('api/user/products/get-all', {
      params: {
        'direction.in': productDirection,
        'balancing.equals': true,
        'active.equals': true,
      },
    }).pipe(map(response => response.map(({ id, shortName, ...restData }) => ({ id, value: id, label: shortName, ...restData }))));
  }

  getStatusDictionary(selectedTab: TabType): Dictionary[] {
    return Helpers.enumToDictionary<AuctionStatus>(AuctionStatus, 'AuctionStatus')
      .filter(({ value }) => {
        return [AuctionStatus.NEW, AuctionStatus.OPEN, AuctionStatus.CLOSED].includes(value);
      })
      .map(({ value, label }) => ({
        value: this.getStatusesForGeneralStatus(value, selectedTab).join(','),
        label,
      }));
  }

  getStatusesForGeneralStatus(status: AuctionStatus, tab: TabType): AuctionStatus[] {
    if (tab === 'capacity-auctions') {
      switch (status) {
        case AuctionStatus.CLOSED:
          return [AuctionStatus.CLOSED_CAPACITY];
        case AuctionStatus.OPEN:
          return [AuctionStatus.OPEN_CAPACITY];
        case AuctionStatus.NEW:
          return [AuctionStatus.NEW_CAPACITY];
      }
    }

    if (tab === 'energy-auctions') {
      switch (status) {
        case AuctionStatus.CLOSED:
          return [AuctionStatus.CLOSED_ENERGY];
        case AuctionStatus.OPEN:
          return [AuctionStatus.OPEN_ENERGY];
        case AuctionStatus.NEW:
          return [AuctionStatus.NEW_ENERGY];
      }
    }

    return [status];
  }

  getTabs(): Tab[] {
    return [
      {
        label: this.translate.instant('auctions.dayAhead.tabs.capacityAuctions'),
        type: 'capacity-auctions',
      },
      {
        label: this.translate.instant('auctions.dayAhead.tabs.energyAuctions'),
        type: 'energy-auctions',
      },
      {
        label: this.translate.instant('auctions.dayAhead.tabs.seriesAuctions'),
        type: 'series-auctions',
      },
      {
        label: this.translate.instant('auctions.dayAhead.tabs.myOffers'),
        type: 'my-offers',
      },
    ];
  }

  hasStatus(status: AuctionStatus, row: AuctionDayAheadDTO): boolean {
    if ([AuctionStatus.OPEN, AuctionStatus.CLOSED, AuctionStatus.NEW].includes(status)) {
      switch (status) {
        case AuctionStatus.OPEN:
          return [AuctionStatus.OPEN_CAPACITY, AuctionStatus.OPEN_ENERGY].includes(row.status);
        case AuctionStatus.CLOSED:
          return [AuctionStatus.CLOSED_CAPACITY, AuctionStatus.CLOSED_ENERGY].includes(row.status);
        case AuctionStatus.NEW:
          return [AuctionStatus.NEW_CAPACITY, AuctionStatus.NEW_ENERGY].includes(row.status);
      }
    }

    return row.status === status;
  }

  tabTypeToType(tab: TabType): AuctionDayAheadType | null {
    switch (tab) {
      case 'energy-auctions':
        return AuctionDayAheadType.ENERGY;
      case 'capacity-auctions':
        return AuctionDayAheadType.CAPACITY;
      case 'series-auctions':
      case 'my-offers':
        return null;
    }
  }

  loadCollection(parameters: DayAheadParameters): Observable<Pageable<AuctionDayAheadDTO>> {
    const { filters, tab, ...params } = parameters;

    const url = tab === 'series-auctions' ? this.seriesUrl : this.url;

    return this.getCollection(url, {
      params: {
        ...params,
        ...filters,
      },
    });
  }

  remove(id: number): Observable<void> {
    return this.delete(`${this.seriesUrl}/${id}`);
  }

  save(data: AuctionDayAheadDTO): Observable<void> {
    return this.post(`${this.seriesUrl}`, this.formatData(data));
  }

  update(data: AuctionDayAheadDTO): Observable<void> {
    return this.put(`${this.seriesUrl}`, this.formatData(data));
  }

  private formatData(data: AuctionDayAheadDTO): AuctionDayAheadDTO {
    const { ...form } = data;

    const correctDeliveryDate = (value: string) => {
      return value ? moment(value).add(1, 'd').toDate().toISOString() : value;
    };

    form.capacityAvailabilityFrom = correctDeliveryDate(form.capacityAvailabilityFrom);
    form.capacityAvailabilityTo = correctDeliveryDate(form.capacityAvailabilityTo);
    form.energyAvailabilityFrom = correctDeliveryDate(form.energyAvailabilityFrom);
    form.energyAvailabilityTo = correctDeliveryDate(form.energyAvailabilityTo);

    return this.formatDateTime(
      {
        ...form,
      },
      [
        'energyGateOpeningTime',
        'energyGateClosureTime',
        'capacityGateOpeningTime',
        'capacityGateClosureTime',
        'capacityAvailabilityFrom',
        'capacityAvailabilityTo',
        'energyAvailabilityFrom',
        'energyAvailabilityTo',
        'firstAuctionDate',
        'lastAuctionDate',
      ]
    );
  }
}
