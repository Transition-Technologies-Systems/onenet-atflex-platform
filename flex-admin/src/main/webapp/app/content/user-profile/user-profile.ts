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
  NEW_FSP_REGISTRATION_APPLICATION_NOTIFICATION: boolean;
  FSP_REGISTRATION_APPLICATION_CONFIRAMTION_NOTIFICATION: boolean;
  FSP_REGISTRATION_CHANGE_NOTIFICATION: boolean;
  FSP_REGISTRATION_ACCEPTED: boolean;
  FSP_REGISTRATION_REJECTED: boolean;
  FSP_REGISTRATION_APPLICATION_WITHDRAWAL: boolean;
  PRODUCT_CREATION: boolean;
  PRODUCT_EDITION: boolean;
  UNIT_CREATION: boolean;
  UNIT_EDITION: boolean;
  SUBPORTFOLIO_CREATION: boolean;
  SUBPORTFOLIO_EDITION: boolean;
  FLEXIBILITY_POTENTIAL_CREATION: boolean;
  FLEXIBILITY_POTENTIAL_EDITION: boolean;
  FLEXIBILITY_POTENTIAL_ASSIGNMENT_TO_REGISTER: boolean;
  SCHEDULING_UNIT_CREATION: boolean;
  SCHEDULING_UNIT_EDITION: boolean;
  SCHEDULING_UNIT_READY_FOR_TESTS_NOTIFICATION: boolean;
  FSP_EDITION: boolean;
}

export enum NotificationObjects {
  FSP_REGISTRATION = 'FSP_REGISTRATION',
  PRODUCT = 'PRODUCT',
  UNIT = 'UNIT',
  SUBPORTFOLIO = 'SUBPORTFOLIO',
  FLEXIBILITY_POTENTIAL = 'FLEXIBILITY_POTENTIAL',
  SCHEDULING_UNIT = 'SCHEDULING_UNIT',
  FSP = 'FSP',
}

export enum NotificationActions {
  CREATION = 'CREATION',
  EDITION = 'EDITION',
  ASSIGNMENT_TO_REGISTER = 'ASSIGNMENT_TO_REGISTER',
  APPLICATION_NOTIFICATION = 'APPLICATION_NOTIFICATION',
  APPLICATION_CONFIRAMTION_NOTIFICATION = 'APPLICATION_CONFIRAMTION_NOTIFICATION',
  CHANGE_NOTIFICATION = 'CHANGE_NOTIFICATION',
  ACCEPTED = 'ACCEPTED',
  REJECTED = 'REJECTED',
  APPLICATION_WITHDRAWAL = 'APPLICATION_WITHDRAWAL',
  READY_FOR_TESTS_NOTIFICATION = 'READY_FOR_TESTS_NOTIFICATION',
}
