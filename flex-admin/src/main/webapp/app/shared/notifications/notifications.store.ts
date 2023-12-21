import { EMPTY, Observable, of } from 'rxjs';
import { catchError, first, switchMap, tap } from 'rxjs/operators';
import { Injectable } from '@angular/core';
import { DefaultParameters, DefaultState, Pageable } from '@app/shared/models';
import { ComponentStore } from '@ngrx/component-store';
import { AlertDTO, NotificationDTO } from './notification';
import { NotificationsService } from './notifications.service';

export interface NotificationsState extends DefaultState<NotificationDTO> {
  undread: number;
}

@Injectable()
export class NotificationsStore extends ComponentStore<NotificationsState> {
  constructor(private service: NotificationsService) {
    super({ undread: 0, data: [], parameters: {}, totalElements: 0 });
  }

  readonly loadCollection = this.effect((effect$: Observable<DefaultParameters>) => {
    return effect$.pipe(
      switchMap((parameters: DefaultParameters) =>
        this.service.loadCollection(parameters).pipe(
          tap(data => this.setData({ data, parameters })),
          catchError(() => EMPTY)
        )
      )
    );
  });

  readonly newData = this.effect((effect$: Observable<void>) => {
    return effect$.pipe(
      switchMap(
        (): Observable<any> =>
          this.parameters$.pipe(
            first(),
            switchMap((parameters: Partial<DefaultParameters>) =>
              this.service.loadCollection(parameters as DefaultParameters).pipe(
                tap(data => this.setData({ data, parameters })),
                catchError(() => EMPTY),
                switchMap(() => of(this.getNotReadCount()))
              )
            )
          )
      )
    );
  });

  readonly getNotReadCount = this.effect((effect$: Observable<void>) => {
    return effect$.pipe(
      switchMap(() =>
        this.service.getNotReadCount().pipe(
          tap(data => this.setUnread(data)),
          catchError(() => EMPTY)
        )
      )
    );
  });

  readonly setData = this.updater(
    (state, { data, parameters }: { data: Pageable<NotificationDTO>; parameters: Partial<DefaultParameters> }) => ({
      ...state,
      parameters,
      data: data.content,
      totalElements: data.totalElements,
    })
  );

  readonly setUnread = this.updater((state, data: number) => ({
    ...state,
    undread: data,
  }));

  readonly undread$: Observable<number> = this.select(state => state.undread);
  readonly data$: Observable<NotificationDTO[] | AlertDTO[]> = this.select(state => state.data);
  readonly totalRecords$: Observable<number> = this.select(state => state.totalElements);
  readonly parameters$: Observable<Partial<DefaultParameters>> = this.select(state => state.parameters);
}
