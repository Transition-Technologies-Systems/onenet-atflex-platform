import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { UntypedFormBuilder, Validators } from '@angular/forms';
import { HttpService } from '@app/core';
import { RequiredNoWhitespaceValidator } from '@app/shared/commons/validators';
import { Role } from '@app/shared/enums';
import { matchValues } from '@app/shared/validators';
import { Observable } from 'rxjs';
import {
  EmailNotification,
  EmailNotificationTemplate,
  NewUserPassword,
  NotificationActions,
  NotificationObjects,
  UserProfileDTO,
} from './user-profile';

const LOGIN_REGEX = '^[a-zA-Z0-9!$&*+=?^_`{|}~.-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*$|^[_.@A-Za-z0-9-]+$';

@Injectable()
export class UserProfileService extends HttpService {
  protected url = 'api/users';
  protected changePasswordUrl = 'api/account/change-password';
  protected emailNotificationsUrl = 'api/user-email-configs';

  constructor(httpClient: HttpClient, private fb: UntypedFormBuilder) {
    super(httpClient);
  }

  createForm(data: UserProfileDTO) {
    return this.fb.group({
      id: [data.id, []],
      firstName: [data.firstName, [RequiredNoWhitespaceValidator, Validators.maxLength(50)]],
      lastName: [data.lastName, [RequiredNoWhitespaceValidator, Validators.maxLength(50)]],
      login: [data.login, [RequiredNoWhitespaceValidator, Validators.maxLength(50), Validators.pattern(LOGIN_REGEX)]],
      email: [{ value: data.email, disabled: true }, [Validators.required, Validators.email]],
      phoneNumber: [data.phoneNumber, [Validators.required, Validators.maxLength(20)]],
      activated: [data.activated],
    });
  }

  createChangePasswordForm() {
    return this.fb.group({
      currentPassword: [null, [Validators.required, Validators.minLength(4), Validators.maxLength(100)]],
      newPassword: [
        null,
        [Validators.required, Validators.minLength(4), Validators.maxLength(100), matchValues('repeatNewPassword', true)],
      ],
      repeatNewPassword: [
        null,
        [
          Validators.required,
          Validators.minLength(4),
          Validators.maxLength(100),
          RequiredNoWhitespaceValidator,
          matchValues('newPassword'),
        ],
      ],
    });
  }

  getProfileData() {
    return this.request('get', `${this.url}/profile-data`);
  }

  updateProfileData(data: UserProfileDTO): Observable<void> {
    return this.put(`${this.url}`, data);
  }

  updateUserPassword(data: NewUserPassword): Observable<void> {
    return this.post(`${this.changePasswordUrl}`, data);
  }

  getNotificationSettingsConfig(): EmailNotificationTemplate[] {
    const { UNIT, FLEXIBILITY_POTENTIAL, SCHEDULING_UNIT, UNIT_ACTIVATION_REMINDER, FSP, SUBPORTFOLIO } = NotificationObjects;
    const { CREATION, EDITION, CERTIFICATION, CERTIFICATION_LOSS, ASSIGNMENT_TO_REGISTER, STATUS_CHANGE, DAY_AHEAD, CMVC } =
      NotificationActions;
    const { ROLE_FLEX_SERVICE_PROVIDER, ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED, ROLE_BALANCING_SERVICE_PROVIDER } = Role;
    return [
      {
        object: UNIT,
        roles: [ROLE_FLEX_SERVICE_PROVIDER, ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED],
        actions: [CREATION, EDITION, CERTIFICATION, CERTIFICATION_LOSS],
      },
      {
        object: SUBPORTFOLIO,
        roles: [ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED],
        actions: [CREATION, EDITION],
      },
      {
        object: FLEXIBILITY_POTENTIAL,
        roles: [ROLE_FLEX_SERVICE_PROVIDER, ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED],
        actions: [CREATION, EDITION, ASSIGNMENT_TO_REGISTER],
      },
      {
        object: SCHEDULING_UNIT,
        roles: [ROLE_BALANCING_SERVICE_PROVIDER],
        actions: [CREATION, EDITION, STATUS_CHANGE, ASSIGNMENT_TO_REGISTER],
      },
      {
        object: UNIT_ACTIVATION_REMINDER,
        roles: [ROLE_FLEX_SERVICE_PROVIDER, ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED],
        actions: [DAY_AHEAD, CMVC],
      },
      {
        object: UNIT_ACTIVATION_REMINDER,
        roles: [ROLE_BALANCING_SERVICE_PROVIDER],
        actions: [DAY_AHEAD],
      },
      {
        object: FSP,
        roles: [ROLE_FLEX_SERVICE_PROVIDER, ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED],
        actions: [EDITION],
      },
    ];
  }

  createEmailNotificationsForm(roles: string[], data?: any) {
    const { ROLE_FLEX_SERVICE_PROVIDER, ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED, ROLE_BALANCING_SERVICE_PROVIDER } = Role;
    let firstAuthorityGroup, secondAuthorityGroup, thirdAuthorityGroup;
    if (roles.some((role: any) => [ROLE_FLEX_SERVICE_PROVIDER, ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED].includes(role))) {
      firstAuthorityGroup = {
        UNIT_CREATION: [false, []],
        UNIT_EDITION: [false, []],
        UNIT_CERTIFICATION: [false, []],
        UNIT_CERTIFICATION_LOSS: [false, []],
        FLEXIBILITY_POTENTIAL_CREATION: [false, []],
        FLEXIBILITY_POTENTIAL_EDITION: [false, []],
        FLEXIBILITY_POTENTIAL_ASSIGNMENT_TO_REGISTER: [false, []],
        UNIT_ACTIVATION_REMINDER_DAY_AHEAD: [false, []],
        UNIT_ACTIVATION_REMINDER_CMVC: [false, []],
        FSP_EDITION: [false, []],
      };
    }
    if (roles.includes(ROLE_BALANCING_SERVICE_PROVIDER)) {
      secondAuthorityGroup = {
        SCHEDULING_UNIT_CREATION: [false, []],
        SCHEDULING_UNIT_EDITION: [false, []],
        SCHEDULING_UNIT_STATUS_CHANGE: [false, []],
        SCHEDULING_UNIT_ASSIGNMENT_TO_REGISTER: [false, []],
        UNIT_ACTIVATION_REMINDER_DAY_AHEAD: [false, []],
      };
    }
    if (roles.includes(ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED)) {
      thirdAuthorityGroup = {
        SUBPORTFOLIO_CREATION: [false, []],
        SUBPORTFOLIO_EDITION: [false, []],
      };
    }
    return this.fb.group({ ...firstAuthorityGroup, ...secondAuthorityGroup, ...thirdAuthorityGroup });
  }

  getUserEmailNotificationsSettings(): Observable<EmailNotification> {
    return this.get(this.emailNotificationsUrl);
  }

  updateUserEmailNotificationSettings(data: EmailNotification): Observable<void> {
    return this.put(this.emailNotificationsUrl, data);
  }
}
