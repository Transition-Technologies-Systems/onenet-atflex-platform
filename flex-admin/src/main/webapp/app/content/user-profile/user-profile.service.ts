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
  protected url = 'flex-server/api/users';
  protected changePasswordUrl = 'flex-server/api/account/change-password';
  protected emailNotificationsUrl = 'flex-server/api/user-email-configs';

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
    const { FSP_REGISTRATION, PRODUCT, UNIT, SUBPORTFOLIO, FLEXIBILITY_POTENTIAL, SCHEDULING_UNIT, FSP } = NotificationObjects;
    const {
      CREATION,
      EDITION,
      ASSIGNMENT_TO_REGISTER,
      APPLICATION_NOTIFICATION,
      APPLICATION_CONFIRAMTION_NOTIFICATION,
      CHANGE_NOTIFICATION,
      ACCEPTED,
      REJECTED,
      APPLICATION_WITHDRAWAL,
      READY_FOR_TESTS_NOTIFICATION,
    } = NotificationActions;
    const { ROLE_ADMIN, ROLE_DISTRIBUTION_SYSTEM_OPERATOR, ROLE_TRANSMISSION_SYSTEM_OPERATOR, ROLE_MARKET_OPERATOR } = Role;
    return [
      {
        object: FSP_REGISTRATION,
        roles: [ROLE_MARKET_OPERATOR],
        actions: [
          APPLICATION_NOTIFICATION,
          APPLICATION_CONFIRAMTION_NOTIFICATION,
          CHANGE_NOTIFICATION,
          ACCEPTED,
          REJECTED,
          APPLICATION_WITHDRAWAL,
        ],
      },
      {
        object: PRODUCT,
        roles: [ROLE_TRANSMISSION_SYSTEM_OPERATOR, ROLE_DISTRIBUTION_SYSTEM_OPERATOR, ROLE_ADMIN],
        actions: [CREATION, EDITION],
      },
      {
        object: UNIT,
        roles: [ROLE_DISTRIBUTION_SYSTEM_OPERATOR, ROLE_ADMIN],
        actions: [CREATION, EDITION],
      },
      {
        object: SUBPORTFOLIO,
        roles: [ROLE_DISTRIBUTION_SYSTEM_OPERATOR, ROLE_ADMIN],
        actions: [CREATION, EDITION],
      },
      {
        object: SUBPORTFOLIO,
        roles: [ROLE_TRANSMISSION_SYSTEM_OPERATOR],
        actions: [EDITION],
      },
      {
        object: FLEXIBILITY_POTENTIAL,
        roles: [ROLE_ADMIN],
        actions: [CREATION, EDITION, ASSIGNMENT_TO_REGISTER],
      },
      {
        object: FLEXIBILITY_POTENTIAL,
        roles: [ROLE_DISTRIBUTION_SYSTEM_OPERATOR],
        actions: [EDITION, ASSIGNMENT_TO_REGISTER],
      },
      {
        object: FLEXIBILITY_POTENTIAL,
        roles: [ROLE_TRANSMISSION_SYSTEM_OPERATOR],
        actions: [EDITION],
      },
      {
        object: SCHEDULING_UNIT,
        roles: [ROLE_TRANSMISSION_SYSTEM_OPERATOR, ROLE_ADMIN],
        actions: [CREATION, EDITION, READY_FOR_TESTS_NOTIFICATION],
      },
      {
        object: FSP,
        roles: [ROLE_MARKET_OPERATOR, ROLE_TRANSMISSION_SYSTEM_OPERATOR, ROLE_ADMIN],
        actions: [EDITION],
      },
    ];
  }

  createEmailNotificationsForm(roles: string[], data?: any) {
    const { ROLE_ADMIN, ROLE_DISTRIBUTION_SYSTEM_OPERATOR, ROLE_TRANSMISSION_SYSTEM_OPERATOR, ROLE_MARKET_OPERATOR } = Role;
    let firstAuthorityGroup, secondAuthorityGroup, thirdAuthorityGroup, fourthAuthorityGroup, fifthAuthorityGroup;
    if (roles.includes(ROLE_MARKET_OPERATOR)) {
      firstAuthorityGroup = {
        NEW_FSP_REGISTRATION_APPLICATION_NOTIFICATION: [false, []],
        FSP_REGISTRATION_APPLICATION_CONFIRAMTION_NOTIFICATION: [false, []],
        FSP_REGISTRATION_CHANGE_NOTIFICATION: [false, []],
        FSP_REGISTRATION_ACCEPTED: [false, []],
        FSP_REGISTRATION_REJECTED: [false, []],
        FSP_REGISTRATION_APPLICATION_WITHDRAWAL: [false, []],
      };
    }

    if (roles.includes(ROLE_ADMIN)) {
      secondAuthorityGroup = {
        PRODUCT_CREATION: [false, []],
        PRODUCT_EDITION: [false, []],
        SUBPORTFOLIO_CREATION: [false, []],
        SUBPORTFOLIO_EDITION: [false, []],
        FLEXIBILITY_POTENTIAL_CREATION: [false, []],
        FLEXIBILITY_POTENTIAL_EDITION: [false, []],
        FLEXIBILITY_POTENTIAL_ASSIGNMENT_TO_REGISTER: [false, []],
      };
    }

    if (roles.includes(ROLE_DISTRIBUTION_SYSTEM_OPERATOR)) {
      secondAuthorityGroup = {
        PRODUCT_CREATION: [false, []],
        PRODUCT_EDITION: [false, []],
        SUBPORTFOLIO_CREATION: [false, []],
        SUBPORTFOLIO_EDITION: [false, []],
        FLEXIBILITY_POTENTIAL_EDITION: [false, []],
        FLEXIBILITY_POTENTIAL_ASSIGNMENT_TO_REGISTER: [false, []],
      };
    }

    if (roles.includes(ROLE_TRANSMISSION_SYSTEM_OPERATOR)) {
      secondAuthorityGroup = {
        PRODUCT_CREATION: [false, []],
        PRODUCT_EDITION: [false, []],
        SUBPORTFOLIO_EDITION: [false, []],
        FLEXIBILITY_POTENTIAL_EDITION: [false, []],
      };
    }

    if (roles.some((role: any) => [ROLE_TRANSMISSION_SYSTEM_OPERATOR, ROLE_ADMIN].includes(role))) {
      thirdAuthorityGroup = {
        SCHEDULING_UNIT_CREATION: [false, []],
        SCHEDULING_UNIT_EDITION: [false, []],
        SCHEDULING_UNIT_READY_FOR_TESTS_NOTIFICATION: [false, []],
      };
    }
    if (roles.some((role: any) => [ROLE_MARKET_OPERATOR, ROLE_TRANSMISSION_SYSTEM_OPERATOR, ROLE_ADMIN].includes(role))) {
      fourthAuthorityGroup = {
        FSP_EDITION: [false, []],
      };
    }
    if (roles.some((role: any) => [ROLE_DISTRIBUTION_SYSTEM_OPERATOR, ROLE_ADMIN].includes(role))) {
      fifthAuthorityGroup = {
        UNIT_CREATION: [false, []],
        UNIT_EDITION: [false, []],
      };
    }
    return this.fb.group({
      ...firstAuthorityGroup,
      ...secondAuthorityGroup,
      ...thirdAuthorityGroup,
      ...fourthAuthorityGroup,
      ...fifthAuthorityGroup,
    });
  }

  getUserEmailNotificationsSettings(): Observable<EmailNotification> {
    return this.get(this.emailNotificationsUrl);
  }

  updateUserEmailNotificationSettings(data: EmailNotification): Observable<void> {
    return this.put(this.emailNotificationsUrl, data);
  }
}
