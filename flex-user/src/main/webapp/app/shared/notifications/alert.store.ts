import { EMPTY, Observable } from 'rxjs';
import { catchError, switchMap, tap } from 'rxjs/operators';

import { Injectable } from '@angular/core';
import { DefaultParameters, DefaultState, Pageable } from '@app/shared/models';
import { ComponentStore } from '@ngrx/component-store';

import { AlertDTO, NotificationDTO } from './notification';
import { NotificationsService } from './notifications.service';

@Injectable()
export class AlertsStore extends ComponentStore<DefaultState<AlertDTO> | DefaultState<NotificationDTO>> {
  constructor(private service: NotificationsService) {
    super({ data: [], parameters: {}, totalElements: 0 });
  }

  readonly loadCollection = this.effect((effect$: Observable<DefaultParameters>) => {
    return effect$.pipe(
      switchMap((parameters: DefaultParameters) =>
        this.service.loadAlertCollection(parameters).pipe(
          tap(data => this.setData({ data, parameters })),
          catchError(() => EMPTY)
        )
      )
    );
  });

  readonly setData = this.updater((state, { data, parameters }: { data: Pageable<AlertDTO>; parameters: Partial<DefaultParameters> }) => ({
    ...state,
    parameters,
    data: data.content,
    totalElements: data.totalElements,
  }));

  readonly data$: Observable<AlertDTO[] | NotificationDTO[]> = this.select(state => state.data);
  readonly totalRecords$: Observable<number> = this.select(state => state.totalElements);
  readonly parameters$: Observable<Partial<DefaultParameters>> = this.select(state => state.parameters);
}
