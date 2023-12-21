import { Language } from '@app/shared/enums';

export interface UserDTO {
  id: number;
  login: string;
  firstName: string;
  lastName: string;
  email: string;
  activated: boolean;
  langKey: Language;
  roles: string[];
  authorities: string[];
  fspId: number;
  fspOwner: boolean;
  phoneNumber: string | any;

  passwordChangeOnFirstLogin: boolean;
  unsuccessfulLoginCount: number;
  lastSuccessfulLoginDate: string;

  createdBy: string;
  createdDate: string;
  lastModifiedBy: string;
  lastModifiedDate: string;
}
