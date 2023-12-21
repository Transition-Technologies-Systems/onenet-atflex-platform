export interface FspUserRegistrationDTO {
  id: number;
  firstName: string;
  lastName: string;
  companyName: string;
  email: string;
  login: string;
  phoneNumber: string;
  status: FspUserRegistrationStatus;

  fspUserId: number;

  createdBy: string;
  createdDate: string;
  lastModifiedBy: string;
  lastModifiedDate: string;
}

export enum FspUserRegistrationStatus {
  NEW = 'NEW',
  CONFIRMED_BY_FSP = 'CONFIRMED_BY_FSP',
  WITHDRAWN_BY_FSP = 'WITHDRAWN_BY_FSP',
  PRE_CONFIRMED_BY_MO = 'PRE_CONFIRMED_BY_MO',
  ACCEPTED_BY_MO = 'ACCEPTED_BY_MO',
  REJECTED_BY_MO = 'REJECTED_BY_MO',
}

export interface FspUserRegistrationFileDTO {
  fspUserRegistrationId: number;
  fspUserRegistrationCommentId: number;
}
