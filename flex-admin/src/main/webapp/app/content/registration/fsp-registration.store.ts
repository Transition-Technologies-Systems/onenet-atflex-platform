import { EMPTY, Observable } from 'rxjs';
import { FspUserRegistrationDTO, TabType } from './fsp-registration';
import { catchError, map, switchMap, tap } from 'rxjs/operators';

import { ComponentStore } from '@ngrx/component-store';
import { FspRegistrationService } from './fsp-registration.service';
import { Injectable } from '@angular/core';
import { DefaultParameters, DefaultState, Pageable } from '@app/shared/models';

export interface FspRegistrationParameters extends Omit<DefaultParameters, 'filters'> {
  tabType: TabType;
}

@Injectable()
export class FspRegistrationStore extends ComponentStore<DefaultState<FspUserRegistrationDTO, FspRegistrationParameters>> {
  constructor(private service: FspRegistrationService) {
    super({ data: [], parameters: {}, totalElements: 0 });
  }

  readonly loadCollection = this.effect((effect$: Observable<FspRegistrationParameters>) => {
    return effect$.pipe(
      switchMap((filters: FspRegistrationParameters) =>
        this.service.loadCollection(filters).pipe(
          tap(data => this.setData(data)),
          catchError(() => EMPTY)
        )
      )
    );
  });

  readonly updateDataInCollection = this.effect((effect$: Observable<number>) => {
    return effect$.pipe(
      switchMap((id: number) =>
        this.service.getFspRegistration(id).pipe(
          tap(data => this.updateOne({ data, id })),
          catchError(() => EMPTY)
        )
      )
    );
  });

  readonly setData = this.updater((state, data: Pageable<FspUserRegistrationDTO>) => ({
    ...state,
    data: data.content,
    totalElements: data.totalElements,
  }));

  readonly updateOne = this.updater((state, { data, id }: { data: FspUserRegistrationDTO; id: number }) => {
    const updateIndex = state.data.findIndex(({ id: dataId }) => dataId === id);

    const stateData = state.data;
    stateData[updateIndex] = data;

    return {
      ...state,
      data: [...stateData],
    };
  });

  readonly totalRecords$: Observable<number> = this.select(state => state.totalElements);
  readonly data$: Observable<FspUserRegistrationDTO[]> = this.select(state => state.data);
  readonly parameters$: Observable<Partial<FspRegistrationParameters>> = this.select(state => state.parameters);

  dataById$(paramId: number): Observable<FspUserRegistrationDTO | undefined> {
    return this.data$.pipe(map((data: FspUserRegistrationDTO[]) => data.find(({ id }) => id === paramId)));
  }
}
