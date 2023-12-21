import { AuctionDayAheadDTO, AuctionsSeriesDTO, TabType } from './day-ahead';
import { EMPTY, Observable } from 'rxjs';
import { catchError, switchMap, tap } from 'rxjs/operators';

import { ComponentStore } from '@ngrx/component-store';
import { DayAheadService } from './day-ahead.service';
import { Injectable } from '@angular/core';
import { DefaultParameters, DefaultState, Pageable } from '@app/shared/models';
import { TableExtendsParameters } from '@app/shared/services';

export interface DayAheadParameters extends DefaultParameters {
  tab?: TabType;
}

@Injectable()
export class DayAheadStore extends ComponentStore<DefaultState<AuctionDayAheadDTO | AuctionsSeriesDTO, DayAheadParameters>> {
  constructor(private service: DayAheadService) {
    super({ data: [], parameters: {}, totalElements: 0 });
  }

  readonly loadCollection = this.effect((effect$: Observable<DayAheadParameters & TableExtendsParameters>) => {
    return effect$.pipe(
      switchMap(({ runAfterGetData, ...parameters }: DayAheadParameters & TableExtendsParameters) => {
        this.setData({ content: [], totalElements: 0 });

        return this.service.loadCollection(parameters).pipe(
          tap(data => {
            this.setData(data);

            if (runAfterGetData) {
              setTimeout(() => runAfterGetData());
            }
          }),
          catchError(() => EMPTY)
        );
      })
    );
  });

  readonly setData = this.updater((state, data: Pageable<AuctionDayAheadDTO>) => ({
    ...state,
    data: data.content,
    totalElements: data.totalElements,
  }));

  readonly updateOne = this.updater((state, data: AuctionDayAheadDTO) => {
    const index = state.data.findIndex((value: AuctionDayAheadDTO | AuctionsSeriesDTO) => data.id === value.id);

    if (index < 0) {
      return {
        ...state,
      };
    }

    const storeData = [...state.data];
    storeData[index] = { ...storeData[index], ...data };

    return {
      ...state,
      data: storeData,
    };
  });

  readonly data$: Observable<Array<AuctionDayAheadDTO | AuctionsSeriesDTO>> = this.select(state => state.data);
  readonly dataIds$: Observable<number[]> = this.select(state => state.data.map(item => item.id));
  readonly totalRecords$: Observable<number> = this.select(state => state.totalElements);
  readonly parameters$: Observable<Partial<DayAheadParameters>> = this.select(state => state.parameters);
}
