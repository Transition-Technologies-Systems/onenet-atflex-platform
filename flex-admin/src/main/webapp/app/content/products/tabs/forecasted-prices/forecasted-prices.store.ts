import * as moment from 'moment';

import { EMPTY, Observable } from 'rxjs';
import { catchError, switchMap, tap } from 'rxjs/operators';

import { ComponentStore } from '@ngrx/component-store';
import { ForecastedPricesFileDTO } from './forecasted-prices';
import { Injectable } from '@angular/core';
import { DefaultParameters, DefaultState, Pageable } from '@app/shared/models';
import { ProductsForecastedPricessService } from './forecasted-prices.service';
import { TableExtendsParameters } from '@app/shared/services';

@Injectable()
export class ProductsForecastedPricesStore extends ComponentStore<DefaultState<ForecastedPricesFileDTO>> {
  constructor(private service: ProductsForecastedPricessService) {
    super({ data: [], parameters: {}, totalElements: 0 });
  }

  readonly loadCollection = this.effect((effect$: Observable<DefaultParameters & TableExtendsParameters>) => {
    return effect$.pipe(
      switchMap(({ runAfterGetData, ...parameters }: DefaultParameters & TableExtendsParameters) =>
        this.service.loadCollection(parameters).pipe(
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

  readonly setData = this.updater((state, data: Pageable<ForecastedPricesFileDTO>) => {
    const currentData = moment().endOf('day');

    return {
      ...state,
      data: data.content.map((value: ForecastedPricesFileDTO) => ({
        ...value,
        canDelete: !moment(value.forecastedPricesDate).isBefore(currentData),
      })),
      totalElements: data.totalElements,
    };
  });

  readonly data$: Observable<ForecastedPricesFileDTO[]> = this.select(state => state.data);
  readonly dataIds$: Observable<number[]> = this.select(state => state.data.map(item => item.id));
  readonly totalRecords$: Observable<number> = this.select(state => state.totalElements);
  readonly parameters$: Observable<Partial<DefaultParameters>> = this.select(state => state.parameters);
}
