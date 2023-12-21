import { EMPTY, Observable } from 'rxjs';
import { catchError, switchMap, tap } from 'rxjs/operators';

import { AuctionOfferDTO } from './offer';
import { AuctionOffersService } from './offers.service';
import { ComponentStore } from '@ngrx/component-store';
import { Injectable } from '@angular/core';
import { TableExtendsParameters } from '@app/shared/services';
import { DefaultParameters, DefaultState } from '@app/shared/models';

export interface AuctionOffersParameters extends DefaultParameters {
  auctionCmvcId: number | undefined;
  auctionDayAheadId: number | undefined;
}

@Injectable()
export class AuctionOffersStore extends ComponentStore<Omit<DefaultState<AuctionOfferDTO, AuctionOffersParameters>, 'totalElements'>> {
  constructor(private service: AuctionOffersService) {
    super({ data: [], parameters: {} });
  }

  readonly loadCollection = this.effect((effect$: Observable<AuctionOffersParameters & TableExtendsParameters>) => {
    return effect$.pipe(
      switchMap(({ runAfterGetData, ...parameters }: AuctionOffersParameters & TableExtendsParameters) =>
        this.service.getOffers(parameters).pipe(
          tap(data => {
            this.setData(data);

            if (runAfterGetData) {
              setTimeout(() => runAfterGetData());
            }
          }),
          catchError(() => EMPTY)
        )
      )
    );
  });

  readonly setData = this.updater((state, data: AuctionOfferDTO[]) => ({
    ...state,
    data,
  }));

  readonly upsertOne = this.updater((state, data: AuctionOfferDTO) => {
    const index = state.data.findIndex((value: AuctionOfferDTO) => data.id === value.id);

    if (index < 0) {
      return {
        ...state,
        data: [...state.data, data],
      };
    }

    const storeData = [...state.data];
    storeData[index] = { ...storeData[index], ...data };

    return {
      ...state,
      data: storeData,
    };
  });

  readonly data$: Observable<AuctionOfferDTO[]> = this.select(state => state.data);
  readonly totalRecords$: Observable<number> = this.select(state => state.data.length);
  readonly dataIds$: Observable<number[]> = this.select(state => state.data.map(item => item.id));
  readonly parameters$: Observable<Partial<AuctionOffersParameters>> = this.select(state => state.parameters);
}
