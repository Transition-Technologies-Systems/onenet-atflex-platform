import { Language } from '@app/shared/enums';

export interface UserProfileDTO {
  id: number;
  login: string;
  firstName: string;
  lastName: string;
  email: string;
  activated: boolean;
  langKey: Language;
  roles: string[];
  authorities: string[];
  phoneNumber: string | any;
  companyName: string;
}

export interface NewUserPassword {
  currentPassword: string;
  newPassword: string;
}

export interface EmailNotificationTemplate {
  object: string;
  roles: string[];
  actions: string[];
}

export interface EmailNotification {
  UNIT_CREATION: boolean;
  UNIT_EDITION: boolean;
  UNIT_CERTIFICATION: boolean;
  UNIT_CERTIFICATION_LOSS: boolean;
  FLEXIBILITY_POTENTIAL_CREATION: boolean;
  FLEXIBILITY_POTENTIAL_EDITION: boolean;
  FLEXIBILITY_POTENTIAL_ASSIGNMENT_TO_REGISTER: boolean;
  UNIT_ACTIVATION_REMINDER_DAY_AHEAD: boolean;
  UNIT_ACTIVATION_REMINDER_CMVC: boolean;
  FSP_EDITION: boolean;
  SCHEDULING_UNIT_CREATION: boolean;
  SCHEDULING_UNIT_EDITION: boolean;
  SCHEDULING_UNIT_STATUS_CHANGE: boolean;
  SCHEDULING_UNIT_ASSIGNMENT_TO_REGISTER: boolean;
  SUBPORTFOLIO_CREATION: boolean;
  SUBPORTFOLIO_EDITION: boolean;
}

export enum NotificationObjects {
  UNIT = 'UNIT',
  FLEXIBILITY_POTENTIAL = 'FLEXIBILITY_POTENTIAL',
  SCHEDULING_UNIT = 'SCHEDULING_UNIT',
  UNIT_ACTIVATION_REMINDER = 'UNIT_ACTIVATION_REMINDER',
  FSP = 'FSP',
  SUBPORTFOLIO = 'SUBPORTFOLIO',
}

export enum NotificationActions {
  CREATION = 'CREATION',
  EDITION = 'EDITION',
  CERTIFICATION = 'CERTIFICATION',
  CERTIFICATION_LOSS = 'CERTIFICATION_LOSS',
  ASSIGNMENT_TO_REGISTER = 'ASSIGNMENT_TO_REGISTER',
  STATUS_CHANGE = 'STATUS_CHANGE',
  DAY_AHEAD = 'DAY_AHEAD',
  CMVC = 'CMVC',
}
