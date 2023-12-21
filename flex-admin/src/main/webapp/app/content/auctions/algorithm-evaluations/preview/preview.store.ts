import { EMPTY, Observable } from 'rxjs';
import { catchError, switchMap, tap } from 'rxjs/operators';
import { ComponentStore } from '@ngrx/component-store';
import { Injectable } from '@angular/core';
import { DefaultParameters, DefaultState, Pageable } from '@app/shared/models';
import { TableExtendsParameters } from '@app/shared/services';
import { AuctionOffersService } from '../../offers';
import { AuctionOfferDTO } from '../../offers/offer';

export interface OffersPreviewState {
  totalElements: number;
  data: AuctionOfferDTO[];
  parameters: Partial<AuctionOffersParameters>;
}

export interface AuctionOffersParameters extends DefaultParameters {
  evaluationId: number | undefined;
  auctionCmvcId: number | undefined;
  auctionDayAheadId: number | undefined;
}

@Injectable()
export class OffersPreviewStore extends ComponentStore<DefaultState<AuctionOfferDTO, AuctionOffersParameters>> {
  constructor(private service: AuctionOffersService) {
    super({ data: [], parameters: {}, totalElements: 0 });
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

  readonly data$: Observable<AuctionOfferDTO[]> = this.select(state => state.data);
  readonly dataIds$: Observable<number[]> = this.select(state => state.data.map(item => item.id));
  readonly totalRecords$: Observable<number> = this.select(state => state.data.length);
  readonly parameters$: Observable<Partial<AuctionOffersParameters>> = this.select(state => state.parameters);
}
