import { AuctionCmvcDTO, Tab } from './cm-vc';
import { Dictionary, LocalizationTypeDTO, Pageable, ProductDTO } from '@app/shared/models';
import { Observable, map } from 'rxjs';

import { AuctionsService } from '../auctions.service';
import { CmVcParameters } from './cm-vc.store';
import { HttpClient } from '@angular/common/http';
import { HttpService } from '@app/core';
import { Injectable } from '@angular/core';
import { LocalizationType } from '@app/shared/enums';
import { TranslateService } from '@ngx-translate/core';

@Injectable()
export class CmVcService extends HttpService {
  protected url = 'api/user/auctions-cmvc';

  constructor(httpClient: HttpClient, private auctionService: AuctionsService, private translate: TranslateService) {
    super(httpClient);
  }

  formatDeliveryDate(row: AuctionCmvcDTO): string {
    return this.auctionService.formatDeliveryDate(row.deliveryDateFrom, row.deliveryDateTo);
  }

  formatLoalizations(row: AuctionCmvcDTO): string {
    if (!row.localization) {
      return '';
    }

    return row.localization.map(({ name, type }) => `${name} (${this.translate.instant(`LocalizationType.${type}`)})`).join(', ');
  }

  getAuction(id: number): Observable<AuctionCmvcDTO> {
    return this.get(`${this.url}/${id}`);
  }

  getLocalizationsDict(): Observable<LocalizationTypeDTO[]> {
    return this.get<LocalizationTypeDTO[]>('api/user/localization-types/get-by-type', {
      params: {
        types: [LocalizationType.COUPLING_POINT_ID, LocalizationType.POWER_STATION_ML_LV_NUMBER],
      },
    }).pipe(
      map((response: LocalizationTypeDTO[]) =>
        response.map((value: LocalizationTypeDTO) => ({
          ...value,
          name: `${value.name} (${this.translate.instant(`LocalizationType.${value.type}`)})`,
        }))
      )
    );
  }

  getOffers(parameters: any): Observable<any[]> {
    const { id, ...params } = parameters;

    return this.get('api/user/auctions-cmvc/offers', {
      params: {
        ...params,
        'auctionCmvc.id': id,
      },
    });
  }

  getProduct(id: number): Observable<ProductDTO> {
    return this.get(`api/user/products/${id}`);
  }

  getProducts(): Observable<Array<Dictionary & { minBidSize: number; maxBidSize: number }>> {
    return this.get<{ id: number; shortName: string; minBidSize: number; maxBidSize: number }[]>('api/user/products/get-all', {
      params: {
        'cmvc.equals': true,
        'active.equals': true,
      },
    }).pipe(map(response => response.map(({ id, shortName, ...restData }) => ({ id, value: id, label: shortName, ...restData }))));
  }

  getTabs(): Tab[] {
    return [
      {
        label: this.translate.instant('auctions.cmvc.tabs.auctions'),
        type: 'auctions',
      },
      {
        label: this.translate.instant('auctions.cmvc.tabs.myOffers'),
        type: 'my-offers',
      },
    ];
  }

  loadCollection(parameters: CmVcParameters): Observable<Pageable<AuctionCmvcDTO>> {
    const { filters, ...params } = parameters;

    return this.getCollection(this.url, {
      params: {
        ...params,
        ...filters,
      },
    });
  }

  remove(id: number): Observable<void> {
    return this.delete(`${this.url}/${id}`);
  }

  save(data: AuctionCmvcDTO): Observable<void> {
    return this.post(`${this.url}`, this.formatData(data));
  }

  update(data: AuctionCmvcDTO): Observable<void> {
    return this.put(`${this.url}`, this.formatData(data));
  }

  private formatData(data: AuctionCmvcDTO): AuctionCmvcDTO {
    const { ...form } = data;

    return this.formatDateTime(
      {
        ...form,
      },
      ['deliveryDateFrom', 'deliveryDateTo', 'gateOpeningTime', 'gateClosureTime']
    );
  }
}
