import { Observable, catchError, of } from 'rxjs';
import { UserScreenConfigDTO } from './view-configuration';

import { HttpService } from '@app/core';
import { Injectable } from '@angular/core';
import { Screen } from '@app/shared/enums';

@Injectable()
export class ViewConfigurationService extends HttpService {
  protected url = 'api/user-screen-configs';

  getConfiguration(screen: Screen): Observable<UserScreenConfigDTO> {
    if (!screen) {
      return of({
        id: -1,
        screen,
        userId: -1,
        screenColumns: [],
      });
    }

    return this.get<UserScreenConfigDTO>(`${this.url}/current-user`, {
      params: {
        screen,
      },
    }).pipe(
      catchError(() =>
        of({
          id: -1,
          screen,
          userId: -1,
          screenColumns: [],
        })
      )
    );
  }

  saveConfiguration(data: UserScreenConfigDTO): Observable<void> {
    return this.post(this.url, data);
  }
}
