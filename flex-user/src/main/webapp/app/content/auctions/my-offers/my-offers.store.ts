import { EMPTY, Observable } from 'rxjs';
import { catchError, switchMap, tap } from 'rxjs/operators';

import { ComponentStore } from '@ngrx/component-store';
import { Injectable } from '@angular/core';
import { MyOffersDTO } from './my-offers';
import { MyOffersService } from './my-offers.service';
import { DefaultParameters, DefaultState, Pageable } from '@app/shared/models';
import { TableExtendsParameters } from '@app/shared/services';

@Injectable()
export class MyOffersStore extends ComponentStore<DefaultState<MyOffersDTO>> {
  constructor(private service: MyOffersService) {
    super({ data: [], parameters: {}, totalElements: 0 });
  }

  readonly loadCollection = this.effect((effect$: Observable<DefaultParameters & TableExtendsParameters>) => {
    return effect$.pipe(
      switchMap(({ runAfterGetData, refresh, ...parameters }: DefaultParameters & TableExtendsParameters) => {
        if (!refresh) {
          this.setData({ content: [], totalElements: 0 });
        }

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

  readonly setData = this.updater((state, data: Pageable<MyOffersDTO>) => ({
    ...state,
    data: data.content,
    totalElements: data.totalElements,
  }));

  readonly data$: Observable<Array<MyOffersDTO>> = this.select(state => state.data);
  readonly dataIds$: Observable<number[]> = this.select(state => state.data.map(item => item.id));
  readonly totalRecords$: Observable<number> = this.select(state => state.totalElements);
  readonly parameters$: Observable<Partial<DefaultParameters>> = this.select(state => state.parameters);
}
