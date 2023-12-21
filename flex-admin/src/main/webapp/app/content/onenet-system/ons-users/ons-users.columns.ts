import { Column } from '@app/shared/models';

export const COLUMNS: Column[] = [
  {
    field: 'id',
  },
  {
    field: 'username',
  },
  {
    field: 'onenetId',
  },
  {
    field: 'email',
  },
  {
    field: 'active',
  },
  {
    field: 'delete',
    export: false,
  },
];
