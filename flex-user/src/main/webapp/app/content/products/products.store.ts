import { EMPTY, Observable } from 'rxjs';
import { DefaultParameters, DefaultState, Pageable, ProductDTO } from '@app/shared/models';
import { catchError, switchMap, tap } from 'rxjs/operators';

import { ComponentStore } from '@ngrx/component-store';
import { Injectable } from '@angular/core';
import { ProductsService } from './products.service';
import { TableExtendsParameters } from '@app/shared/services';

@Injectable()
export class ProductsStore extends ComponentStore<DefaultState<ProductDTO>> {
  constructor(private service: ProductsService) {
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

  readonly setData = this.updater((state, data: Pageable<ProductDTO>) => ({
    ...state,
    data: data.content,
    totalElements: data.totalElements,
  }));

  readonly data$: Observable<ProductDTO[]> = this.select(state => state.data);
  readonly dataIds$: Observable<number[]> = this.select(state => state.data.map(item => item.id));
  readonly totalRecords$: Observable<number> = this.select(state => state.totalElements);
  readonly parameters$: Observable<Partial<DefaultParameters>> = this.select(state => state.parameters);
}
