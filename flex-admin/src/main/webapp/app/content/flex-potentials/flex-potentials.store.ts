import { EMPTY, Observable } from 'rxjs';
import { DefaultParameters, DefaultState, FlexPotentialDTO, Pageable } from '@app/shared/models';
import { catchError, switchMap, tap } from 'rxjs/operators';

import { ComponentStore } from '@ngrx/component-store';
import { FlexPotentialsService } from './flex-potentials.service';
import { Injectable } from '@angular/core';
import { TableExtendsParameters } from '@app/shared/services';

export interface FlexPotentialsParameters extends DefaultParameters {
  'isRegister.equals'?: boolean;
}

@Injectable()
export class FlexPotentialsStore extends ComponentStore<DefaultState<FlexPotentialDTO, FlexPotentialsParameters>> {
  constructor(private service: FlexPotentialsService) {
    super({ data: [], parameters: {}, totalElements: 0 });
  }

  readonly loadCollection = this.effect((effect$: Observable<FlexPotentialsParameters & TableExtendsParameters>) => {
    return effect$.pipe(
      switchMap(({ runAfterGetData, ...parameters }: FlexPotentialsParameters & TableExtendsParameters) =>
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

  readonly setData = this.updater((state, data: Pageable<FlexPotentialDTO>) => ({
    ...state,
    data: data.content,
    totalElements: data.totalElements,
  }));

  readonly data$: Observable<FlexPotentialDTO[]> = this.select(state => state.data);
  readonly dataIds$: Observable<number[]> = this.select(state => state.data.map(item => item.id));
  readonly totalRecords$: Observable<number> = this.select(state => state.totalElements);
  readonly parameters$: Observable<Partial<FlexPotentialsParameters>> = this.select(state => state.parameters);
}
