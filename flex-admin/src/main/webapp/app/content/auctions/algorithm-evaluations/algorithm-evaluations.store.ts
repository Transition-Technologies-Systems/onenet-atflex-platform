import { EMPTY, Observable } from 'rxjs';
import { catchError, switchMap, tap } from 'rxjs/operators';

import { AlgorithmEvaluationDTO, AlgorithmStatus } from './algorithm-evaluation';
import { AlgorithmEvaluationsService } from './algorithm-evaluations.service';
import { ComponentStore } from '@ngrx/component-store';
import { Injectable } from '@angular/core';
import { DefaultParameters, DefaultState, Pageable } from '@app/shared/models';
import { TableExtendsParameters } from '@app/shared/services';
import { MinimalDTO } from '../../../shared/models/minimal';

@Injectable()
export class AlgorithmEvaluationsStore extends ComponentStore<DefaultState<AlgorithmEvaluationDTO>> {
  constructor(private service: AlgorithmEvaluationsService) {
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

  readonly setData = this.updater((state, data: Pageable<AlgorithmEvaluationDTO>) => ({
    ...state,
    data: data.content,
    totalElements: data.totalElements,
  }));

  readonly upsertOne = this.updater((state, data: AlgorithmEvaluationDTO) => {
    const index = state.data.findIndex((value: AlgorithmEvaluationDTO) => data.evaluationId === value.evaluationId);

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

  readonly upsertStatus = this.updater((state, data: MinimalDTO<number, AlgorithmStatus>) => {
    const index = state.data.findIndex((value: AlgorithmEvaluationDTO) => data.id === value.evaluationId);

    if (index < 0) {
      return {
        ...state,
        data: [...state.data],
      };
    }

    const storeData = [...state.data];
    storeData[index].status = data.value;

    return {
      ...state,
      data: storeData,
    };
  });

  readonly data$: Observable<Array<AlgorithmEvaluationDTO>> = this.select(state => state.data);
  readonly totalRecords$: Observable<number> = this.select(state => state.totalElements);
  readonly parameters$: Observable<Partial<DefaultParameters>> = this.select(state => state.parameters);
}
