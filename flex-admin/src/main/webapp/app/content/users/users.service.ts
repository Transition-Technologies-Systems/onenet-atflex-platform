import { Observable } from 'rxjs';

import { Injectable } from '@angular/core';
import { HttpService } from '@app/core';
import { DefaultParameters, Pageable, UserDTO } from '@app/shared/models';

@Injectable()
export class UsersService extends HttpService {
  protected url = 'flex-server/api/users';

  loadCollection(parameters: DefaultParameters): Observable<Pageable<UserDTO>> {
    const { filters, ...params } = parameters;

    return this.getCollection<UserDTO>(this.url, {
      params: {
        ...params,
        ...filters,
      },
    });
  }

  remove(id: number): Observable<void> {
    return this.delete(`${this.url}/${id}`);
  }

  save(form: UserDTO): Observable<void> {
    const data = {
      ...form,
      phoneNumber: form.phoneNumber?.e164Number,
    };

    return this.post(`${this.url}`, data);
  }

  update(id: number, form: UserDTO): Observable<void> {
    const data = {
      ...form,
      phoneNumber: form.phoneNumber?.e164Number,
    };

    return this.put(`${this.url}`, data);
  }
}
