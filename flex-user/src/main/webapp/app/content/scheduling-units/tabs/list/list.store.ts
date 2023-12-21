import { EMPTY, Observable } from 'rxjs';
import { catchError, switchMap, tap } from 'rxjs/operators';

import { ComponentStore } from '@ngrx/component-store';
import { Injectable } from '@angular/core';
import { DefaultParameters, DefaultState, Pageable } from '@app/shared/models';
import { SchedulingUnitDTO } from '../../scheduling-units';
import { SchedulingUnitsService } from '../../scheduling-units.service';
import { TableExtendsParameters } from '@app/shared/services';

@Injectable()
export class SchedulingUnitsListStore extends ComponentStore<DefaultState<SchedulingUnitDTO>> {
  constructor(private service: SchedulingUnitsService) {
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

  readonly setData = this.updater((state, data: Pageable<SchedulingUnitDTO>) => ({
    ...state,
    data: data.content,
    totalElements: data.totalElements,
  }));

  readonly data$: Observable<SchedulingUnitDTO[]> = this.select(state => state.data);
  readonly dataIds$: Observable<number[]> = this.select(state => state.data.map(item => item.id));
  readonly totalRecords$: Observable<number> = this.select(state => state.totalElements);
  readonly parameters$: Observable<Partial<DefaultParameters>> = this.select(state => state.parameters);
}
