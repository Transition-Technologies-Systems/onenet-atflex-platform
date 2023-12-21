import { Dictionary, UserDTO } from '@app/shared/models';
import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';

import { HttpClient } from '@angular/common/http';
import { HttpService } from '@app/core';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { RequiredNoWhitespaceValidator } from '@app/shared/commons/validators';
import { Role } from '@app/shared/enums';
import { map } from 'rxjs/operators';
import { matchValues } from '@app/shared/validators';

const LOGIN_REGEX = '^[a-zA-Z0-9!$&*+=?^_`{|}~.-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*$|^[_.@A-Za-z0-9-]+$';

@Injectable()
export class UsersDialogService extends HttpService {
  constructor(httpClient: HttpClient, private fb: UntypedFormBuilder) {
    super(httpClient);
  }

  createForm(data: Partial<UserDTO> = {}, mode: 'add' | 'edit'): UntypedFormGroup {
    const [role = null] = data.roles || [];

    return this.fb.group({
      id: [{ value: data.id, disabled: true }],
      firstName: [data.firstName, [RequiredNoWhitespaceValidator, Validators.maxLength(50)]],
      lastName: [data.lastName, [RequiredNoWhitespaceValidator, Validators.maxLength(50)]],
      login: [
        { value: data.login, disabled: mode === 'edit' },
        [RequiredNoWhitespaceValidator, Validators.maxLength(50), Validators.pattern(LOGIN_REGEX)],
      ],
      password: [
        null,
        !!data.id ? null : [Validators.required, matchValues('confirmPassword', true), Validators.minLength(4), Validators.maxLength(100)],
      ],
      confirmPassword: [null, !!data.id ? null : [RequiredNoWhitespaceValidator, matchValues('password')]],
      role: [{ value: role, disabled: !!data.fspOwner }, [Validators.required]],
      fspId: [{ value: data.fspId, disabled: !!data.fspOwner }, role === Role.ROLE_FLEX_SERVICE_PROVIDER ? Validators.required : null],
      email: [{ value: data.email, disabled: mode === 'edit' }, [Validators.required, Validators.email]],
      phoneNumber: [data.phoneNumber, [Validators.required, Validators.maxLength(20)]],
      activated: [data.activated],
      passwordChangeOnFirstLogin: [false],
      passwordSetByUser: [false],
    });
  }

  getCompanies(role?: Role): Observable<Dictionary[]> {
    return this.get<{ id: number; companyName: string }[]>('flex-server/api/fsps/get-company', {
      params: {
        roles: role ?? [Role.ROLE_FLEX_SERVICE_PROVIDER, Role.ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED],
      },
    }).pipe(map(response => response.map(({ id, companyName }) => ({ id, value: id, label: companyName }))));
  }
}
