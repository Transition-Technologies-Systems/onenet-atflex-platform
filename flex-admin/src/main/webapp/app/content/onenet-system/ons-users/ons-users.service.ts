import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { HttpService } from '@app/core';
import { DefaultParameters, Pageable } from '@app/shared/models';
import { Observable } from 'rxjs';
import { OnsUserDTO } from './ons-users';

@Injectable()
export class OnsUsersService extends HttpService {
  protected url = 'flex-onenet/api/admin/users';

  constructor(httpClient: HttpClient) {
    super(httpClient);
  }

  loadCollection(parameters: DefaultParameters): Observable<Pageable<OnsUserDTO>> {
    const { filters, ...params } = parameters;

    return this.getCollection(this.url, {
      params: {
        ...params,
        ...filters,
      },
    });
  }

  addUser(user: OnsUserDTO): Observable<void> {
    return this.post(`${this.url}/add`, user);
  }

  remove(id: number): Observable<void> {
    return this.delete(`${this.url}/${id}/remove`);
  }

  setActiveUser(id: number): Observable<void> {
    return this.put(`${this.url}/${id}/set-active`, {});
  }
}
