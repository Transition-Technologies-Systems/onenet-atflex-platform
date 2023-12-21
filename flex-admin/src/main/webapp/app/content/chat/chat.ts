import { Role } from '@app/shared/enums';

export interface ChatRespondentDTO {
  id: number;
  name: string;
  role: Role;
}
