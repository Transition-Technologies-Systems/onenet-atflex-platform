import { MinimalDTO } from '@app/shared/models/minimal';

export interface NotificationDTO {
  id: number;
  eventType: NotificationEvent;
  createdDate: string;
  params: { [key: string]: { value: string; object: string | null } };
  userId: number;
  read: boolean;
  users: MinimalDTO<number, string>[];
  usersIdsRead: MinimalDTO<number, boolean>[];
}

export interface AlertDTO {
  id: number;
  read: boolean;
  createdDate: string;
  errorCode: string;
  event: string;
  login: string;
  objectId: number;

  appModuleName: string;
  httpRequestUriPath: string;
  httpResponseStatus: string;
}

export enum NotificationEvent {
  FSP_USER_REGISTRATION_CONFIRMED_BY_FSP = 'FSP_USER_REGISTRATION_CONFIRMED_BY_FSP',
  FSP_USER_REGISTRATION_WITHDRAWN_BY_FSP = 'FSP_USER_REGISTRATION_WITHDRAWN_BY_FSP',
  FSP_USER_REGISTRATION_ACCEPTED_BY_MO = 'FSP_USER_REGISTRATION_ACCEPTED_BY_MO',
  FSP_USER_REGISTRATION_REJECTED_BY_MO = 'FSP_USER_REGISTRATION_REJECTED_BY_MO',
  SCHEDULING_UNIT_PROPOSAL_TO_FSP = 'SCHEDULING_UNIT_PROPOSAL_TO_FSP',
  SCHEDULING_UNIT_PROPOSAL_TO_BSP = 'SCHEDULING_UNIT_PROPOSAL_TO_BSP',
  SCHEDULING_UNIT_READY_FOR_TESTS = 'SCHEDULING_UNIT_READY_FOR_TESTS',
  FSP_USER_REGISTRATION_UPDATED = 'FSP_USER_REGISTRATION_UPDATED',
  FSP_USER_REGISTRATION_NEW = 'FSP_USER_REGISTRATION_NEW',
  UNIT_LOST_CERTIFICATION = 'UNIT_LOST_CERTIFICATION',
  UNIT_HAS_BEEN_CERTIFIED = 'UNIT_HAS_BEEN_CERTIFIED',
  FP_UPDATED = 'FP_UPDATED',
  FP_DELETED = 'FP_DELETED',
  FP_CREATED = 'FP_CREATED',
  BID_IMPORT = 'BID_IMPORT',
  DISAGGREGATION_COMPLETED = 'DISAGGREGATION_COMPLETED',
  DISAGGREGATION_FAILED = 'DISAGGREGATION_FAILED',
  CONNECTION_TO_ALGORITHM_SERVICE_LOST = 'CONNECTION_TO_ALGORITHM_SERVICE_LOST',
}

export enum NotificationParam {
  ID = 'ID',
  LOGIN = 'LOGIN',
  COMPANY = 'COMPANY',
}

export enum AlertEvent {
  USER_CREATED = 'USER_CREATED',
  USER_UPDATED = 'USER_UPDATED',
  USER_DELETED = 'USER_DELETED',
  USER_CREATED_ERROR = 'USER_CREATED_ERROR',
  USER_UPDATED_ERROR = 'USER_UPDATED_ERROR',
  USER_DELETED_ERROR = 'USER_DELETED_ERROR',
  PRODUCT_CREATED = 'PRODUCT_CREATED',
  PRODUCT_UPDATED = 'PRODUCT_UPDATED',
  PRODUCT_DELETED = 'PRODUCT_DELETED',
  PRODUCT_CREATED_ERROR = 'PRODUCT_CREATED_ERROR',
  PRODUCT_UPDATED_ERROR = 'PRODUCT_UPDATED_ERROR',
  PRODUCT_DELETED_ERROR = 'PRODUCT_DELETED_ERROR',
  FP_CREATED = 'FP_CREATED',
  FP_UPDATED = 'FP_UPDATED',
  FP_DELETED = 'FP_DELETED',
  FP_CREATED_ERROR = 'FP_CREATED_ERROR',
  FP_UPDATED_ERROR = 'FP_UPDATED_ERROR',
  FP_DELETED_ERROR = 'FP_DELETED_ERROR',
  UNIT_CREATED = 'UNIT_CREATED',
  UNIT_UPDATED = 'UNIT_UPDATED',
  UNIT_DELETED = 'UNIT_DELETED',
  UNIT_CREATED_ERROR = 'UNIT_CREATED_ERROR',
  UNIT_UPDATED_ERROR = 'UNIT_UPDATED_ERROR',
  UNIT_DELETED_ERROR = 'UNIT_DELETED_ERROR',
  FSP_CREATED = 'FSP_CREATED',
  FSP_UPDATED = 'FSP_UPDATED',
  FSP_DELETED = 'FSP_DELETED',
  FSP_CREATED_ERROR = 'FSP_CREATED_ERROR',
  FSP_UPDATED_ERROR = 'FSP_UPDATED_ERROR',
  FSP_DELETED_ERROR = 'FSP_DELETED_ERROR',

  SCHEDULING_UNIT_CREATED = 'SCHEDULING_UNIT_CREATED',
  SCHEDULING_UNIT_UPDATED = 'SCHEDULING_UNIT_UPDATED',
  SCHEDULING_UNIT_DELETED = 'SCHEDULING_UNIT_DELETED',
  SCHEDULING_UNIT_CREATED_ERROR = 'SCHEDULING_UNIT_CREATED_ERROR',
  SCHEDULING_UNIT_UPDATED_ERROR = 'SCHEDULING_UNIT_UPDATED_ERROR',
  SCHEDULING_UNIT_DELETED_ERROR = 'SCHEDULING_UNIT_DELETED_ERROR',

  SUBPORTFOLIO_CREATED = 'SUBPORTFOLIO_CREATED',
  SUBPORTFOLIO_UPDATED = 'SUBPORTFOLIO_UPDATED',
  SUBPORTFOLIO_DELETED = 'SUBPORTFOLIO_DELETED',
  SUBPORTFOLIO_CREATED_ERROR = 'SUBPORTFOLIO_CREATED_ERROR',
  SUBPORTFOLIO_UPDATED_ERROR = 'SUBPORTFOLIO_UPDATED_ERROR',
  SUBPORTFOLIO_DELETED_ERROR = 'SUBPORTFOLIO_DELETED_ERROR',

  DER_TYPE_CREATED = 'DER_TYPE_CREATED',
  DER_TYPE_UPDATED = 'DER_TYPE_UPDATED',
  DER_TYPE_DELETED = 'DER_TYPE_DELETED',
  DER_TYPE_CREATED_ERROR = 'DER_TYPE_CREATED_ERROR',
  DER_TYPE_UPDATED_ERROR = 'DER_TYPE_UPDATED_ERROR',
  DER_TYPE_DELETED_ERROR = 'DER_TYPE_DELETED_ERROR',
  UNEXPECTED_ERROR = 'UNEXPECTED_ERROR',
}
