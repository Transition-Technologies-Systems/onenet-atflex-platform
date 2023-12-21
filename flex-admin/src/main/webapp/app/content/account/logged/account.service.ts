import { Observable } from 'rxjs';

import { Injectable } from '@angular/core';
import { HttpService } from '@app/core';

@Injectable()
export class AccountService extends HttpService {
  changePassword({ currentPassword, password }: { currentPassword: string; password: string }): Observable<void> {
    return this.post('flex-server/api/account/change-password', {
      newPassword: password,
      currentPassword,
    });
  }
}
