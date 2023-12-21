import { EMPTY, Observable } from 'rxjs';
import { PartnershipDTO, TabType } from './partnership';
import { catchError, switchMap, tap } from 'rxjs/operators';

import { ComponentStore } from '@ngrx/component-store';
import { Injectable } from '@angular/core';
import { DefaultParameters, DefaultState, Pageable } from '@app/shared/models';
import { PartnershipService } from './partnership.service';

export interface PartnershipParameters extends DefaultParameters {
  tabType: TabType;
}

@Injectable()
export class PartnershipStore extends ComponentStore<DefaultState<PartnershipDTO, PartnershipParameters>> {
  constructor(private service: PartnershipService) {
    super({ data: [], parameters: {}, totalElements: 0 });
  }

  readonly loadCollection = this.effect((effect$: Observable<PartnershipParameters>) => {
    return effect$.pipe(
      switchMap((filters: PartnershipParameters) => {
        this.setData({ content: [], totalElements: 0 });

        return this.service.loadCollection(filters).pipe(
          tap(data => this.setData(data)),
          catchError(() => EMPTY)
        );
      })
    );
  });

  readonly setData = this.updater((state, data: Pageable<PartnershipDTO>) => ({
    ...state,
    data: data.content,
    totalElements: data.totalElements,
  }));

  readonly totalRecords$: Observable<number> = this.select(state => state.totalElements);
  readonly data$: Observable<PartnershipDTO[]> = this.select(state => state.data);
  readonly parameters$: Observable<Partial<PartnershipParameters>> = this.select(state => state.parameters);
}
