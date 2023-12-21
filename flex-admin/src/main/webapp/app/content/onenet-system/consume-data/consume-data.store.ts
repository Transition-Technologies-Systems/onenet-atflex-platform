import { Injectable } from '@angular/core';
import { DefaultParameters, DefaultState, Pageable } from '@app/shared/models';
import { TableExtendsParameters } from '@app/shared/services';
import { ComponentStore } from '@ngrx/component-store';
import { catchError, EMPTY, Observable, switchMap, tap } from 'rxjs';
import { ConsumeDataDTO } from './consume-data';
import { ConsumeDataService } from './consume-data.service';

@Injectable()
export class ConsumeDataStore extends ComponentStore<DefaultState<ConsumeDataDTO>> {
  constructor(private service: ConsumeDataService) {
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

  readonly setData = this.updater((state, data: Pageable<ConsumeDataDTO>) => ({
    ...state,
    data: data.content,
    totalElements: data.totalElements,
  }));

  readonly data$: Observable<ConsumeDataDTO[]> = this.select(state => state.data);
  readonly dataIds$: Observable<number[]> = this.select(state => state.data.map(item => item.id));
  readonly totalRecords$: Observable<number> = this.select(state => state.totalElements);
  readonly parameters$: Observable<Partial<DefaultParameters>> = this.select(state => state.parameters);
}
