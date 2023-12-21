import { EMPTY, Observable } from 'rxjs';
import { DefaultParameters, DefaultState, FspDTO, Pageable } from '@app/shared/models';
import { catchError, switchMap, tap } from 'rxjs/operators';

import { ComponentStore } from '@ngrx/component-store';
import { FspsService } from './fsps.service';
import { Injectable } from '@angular/core';
import { TableExtendsParameters } from '@app/shared/services';

export interface FspsParameters extends DefaultParameters {
  bsp?: boolean;
}

@Injectable()
export class FspsStore extends ComponentStore<DefaultState<FspDTO, FspsParameters>> {
  constructor(private service: FspsService) {
    super({ data: [], parameters: {}, totalElements: 0 });
  }

  readonly loadCollection = this.effect((effect$: Observable<FspsParameters & TableExtendsParameters>) => {
    return effect$.pipe(
      switchMap(({ runAfterGetData, ...parameters }: FspsParameters & TableExtendsParameters) =>
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

  readonly setData = this.updater((state, data: Pageable<FspDTO>) => ({
    ...state,
    data: data.content,
    totalElements: data.totalElements,
  }));

  readonly data$: Observable<FspDTO[]> = this.select(state => state.data);
  readonly dataIds$: Observable<number[]> = this.select(state => state.data.map(item => item.id));
  readonly totalRecords$: Observable<number> = this.select(state => state.totalElements);
  readonly parameters$: Observable<Partial<FspsParameters>> = this.select(state => state.parameters);
}
