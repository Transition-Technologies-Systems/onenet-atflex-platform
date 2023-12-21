import { Screen } from '@app/shared/enums';

export interface UserScreenConfigDTO {
  id?: number;
  screen: Screen;
  userId?: number;
  screenColumns: ScreenColumnDTO[];
}

export interface ScreenColumnDTO {
  columnName: string;
  visible: boolean;
  orderNr: number;
}
