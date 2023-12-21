import { EMPTY, Observable } from 'rxjs';
import { catchError, switchMap, tap } from 'rxjs/operators';

import { ComponentStore } from '@ngrx/component-store';
import { Injectable } from '@angular/core';
import { DefaultParameters, DefaultState, Pageable } from '@app/shared/models';
import { TableExtendsParameters } from '@app/shared/services';
import { UnitDTO } from '../../unit';
import { UnitsService } from '../../units.service';

@Injectable()
export class UnitsListStore extends ComponentStore<DefaultState<UnitDTO>> {
  constructor(private service: UnitsService) {
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

  readonly setData = this.updater((state, data: Pageable<UnitDTO>) => ({
    ...state,
    data: data.content,
    totalElements: data.totalElements,
  }));

  readonly upsertOne = this.updater((state, data: UnitDTO) => {
    const index = state.data.findIndex((value: UnitDTO) => data.id === value.id);

    if (index < 0) {
      return {
        ...state,
        data: [data, ...state.data],
      };
    }

    const storeData = [...state.data];
    storeData[index] = { ...storeData[index], ...data };

    return {
      ...state,
      data: storeData,
    };
  });

  readonly data$: Observable<UnitDTO[]> = this.select(state => state.data);
  readonly dataIds$: Observable<number[]> = this.select(state => state.data.map(item => item.id));
  readonly totalRecords$: Observable<number> = this.select(state => state.totalElements);
  readonly parameters$: Observable<Partial<DefaultParameters>> = this.select(state => state.parameters);
}
