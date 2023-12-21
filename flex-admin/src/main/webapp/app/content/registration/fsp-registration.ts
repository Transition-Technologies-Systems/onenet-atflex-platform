import { FspUserRegistrationStatus, Role } from '@app/shared/enums';

export interface Tab {
  label: string;
  type: TabType;
}

export type TabType = 'active' | 'inactive';

export interface FspUserRegistrationDTO {
  id: number;
  firstName: string;
  lastName: string;
  companyName: string;
  email: string;
  login: string;
  phoneNumber: string;
  status: FspUserRegistrationStatus;
  userTargetRole: Role;

  fspUserId: number;
  readByAdmin: boolean;

  createdBy: string;
  createdDate: string;
  lastModifiedBy: string;
  lastModifiedDate: string;
}

export interface FspUserRegistrationFileDTO {
  fspUserRegistrationId: number;
  fspUserRegistrationCommentId: number;
}
