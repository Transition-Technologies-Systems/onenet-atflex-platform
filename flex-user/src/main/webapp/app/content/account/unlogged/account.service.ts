import { Observable, of } from 'rxjs';

import { Injectable } from '@angular/core';
import { HttpService } from '@app/core';
import { FspUserRegistrationDTO } from '@app/shared/models';

@Injectable()
export class AccountService extends HttpService {
  activateAccount(key: string, password: string, login: string): Observable<void> {
    return this.post(`api/account/activate-and-set-password`, { key, newPassword: password, newLogin: login });
  }

  getAccountDataByKey(key: string): Observable<FspUserRegistrationDTO> {
    return this.get(`api/fsp-user-registration/user/fsp/by-user-key`, { params: { key } });
  }

  resetPassword(data: { password: string; key: string }): Observable<void> {
    return this.post('api/account/reset-password/finish', {
      newPassword: data.password,
      key: data.key,
    });
  }
}
