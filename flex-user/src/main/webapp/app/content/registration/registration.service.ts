import { ContentType, Role } from '@app/shared/enums';
import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';

import { HttpClient } from '@angular/common/http';
import { HttpService } from '@app/core';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { RequiredNoWhitespaceValidator } from '@app/shared/commons/validators';

@Injectable()
export class RegistrationService extends HttpService {
  protected url = 'api/fsp-user-registration';

  constructor(private fb: UntypedFormBuilder, httpClient: HttpClient) {
    super(httpClient);
  }

  createForm(): UntypedFormGroup {
    return this.fb.group({
      userTargetRole: [Role.ROLE_FLEX_SERVICE_PROVIDER, Validators.required],
      firstName: [null, [RequiredNoWhitespaceValidator, Validators.maxLength(50)]],
      lastName: [null, [RequiredNoWhitespaceValidator, Validators.maxLength(50)]],
      companyName: [null, [RequiredNoWhitespaceValidator, Validators.maxLength(254)]],
      email: [null, [Validators.required, Validators.email]],
      phoneNumber: [null, [Validators.required, Validators.maxLength(20)]],

      rulesConfirmation: [false, Validators.requiredTrue],
      rodoConfirmation: [false, Validators.requiredTrue],
    });
  }

  confirm(key: string): Observable<{ id: number }> {
    return this.get('api/fsp-user-registration/user/fsp/confirm-by-key', {
      params: { key },
    });
  }

  getRules(type: 'RULES' | 'RODO'): Observable<{ base64StringData: string; fileName: string; fileExtension: ContentType }> {
    return this.get(`api/users/rules/${type}`);
  }

  withdraw(key: string, removeDbEntry: boolean): Observable<void> {
    return this.get('api/fsp-user-registration/user/fsp/withdraw-by-key', {
      params: {
        removeDbEntry,
        key,
      },
    });
  }

  register(form: any, files: File[]): Observable<void> {
    const formData = new FormData();

    formData.append(
      'fspUserRegistrationDTO',
      new Blob(
        [
          JSON.stringify({
            ...form,
            phoneNumber: form.phoneNumber?.e164Number,
          }),
        ],
        {
          type: 'application/json',
        }
      )
    );

    (files || []).forEach((file: File) => {
      formData.append('files', file);
    });

    return this.post('api/fsp-user-registration/user/fsp/create', formData);
  }
}
