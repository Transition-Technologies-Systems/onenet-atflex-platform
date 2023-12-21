import { EMPTY, Observable } from 'rxjs';
import { catchError, switchMap, tap } from 'rxjs/operators';

import { AuctionCmvcDTO } from './cm-vc';
import { CmVcService } from './cm-vc.service';
import { ComponentStore } from '@ngrx/component-store';
import { Injectable } from '@angular/core';
import { DefaultParameters, DefaultState, Pageable } from '@app/shared/models';
import { TableExtendsParameters } from '@app/shared/services';

export interface CmVcParameters extends DefaultParameters {
  bsp?: boolean;
}

@Injectable()
export class CmVcStore extends ComponentStore<DefaultState<AuctionCmvcDTO, CmVcParameters>> {
  constructor(private service: CmVcService) {
    super({ data: [], parameters: {}, totalElements: 0 });
  }

  readonly loadCollection = this.effect((effect$: Observable<CmVcParameters & TableExtendsParameters>) => {
    return effect$.pipe(
      switchMap(({ runAfterGetData, ...parameters }: CmVcParameters & TableExtendsParameters) =>
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

  readonly setData = this.updater((state, data: Pageable<AuctionCmvcDTO>) => ({
    ...state,
    data: data.content,
    totalElements: data.totalElements,
  }));

  readonly updateOne = this.updater((state, data: AuctionCmvcDTO) => {
    const index = state.data.findIndex((value: AuctionCmvcDTO) => data.id === value.id);

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

  readonly data$: Observable<AuctionCmvcDTO[]> = this.select(state => state.data);
  readonly dataIds$: Observable<number[]> = this.select(state => state.data.map(item => item.id));
  readonly totalRecords$: Observable<number> = this.select(state => state.totalElements);
  readonly parameters$: Observable<Partial<CmVcParameters>> = this.select(state => state.parameters);
}
