import { Observable } from 'rxjs';

import { Injectable } from '@angular/core';
import { HttpService } from '@app/core';

@Injectable()
export class AccountService extends HttpService {
  resetPassword(data: { password: string; key: string }): Observable<void> {
    return this.post('flex-server/api/account/reset-password/finish', {
      newPassword: data.password,
      key: data.key,
    });
  }
}
