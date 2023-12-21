import { HttpClient } from '@angular/common/http';
import { HttpService } from '@app/core';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { TranslateService } from '@ngx-translate/core';

@Injectable()
export class LoginService extends HttpService {
  constructor(httpClient: HttpClient, private translate: TranslateService) {
    super(httpClient);
  }

  login(credentials: any): Observable<{ id_token: string }> {
    return this.post('flex-server/api/authenticate', { ...credentials, rememberMe: false });
  }

  forgotPassword(email: string): Observable<void> {
    return this.post('flex-server/api/account/reset-password/init', email, {
      params: {
        langKey: this.translate.currentLang,
      },
    });
  }
}
