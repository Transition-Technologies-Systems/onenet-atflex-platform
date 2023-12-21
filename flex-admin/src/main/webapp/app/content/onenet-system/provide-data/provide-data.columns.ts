import { Column } from '@app/shared/models';

export const COLUMNS: Column[] = [
  {
    field: 'id',
  },
  {
    field: 'title',
  },
  {
    field: 'onenetId',
  },
  {
    field: 'businessObject',
  },
  {
    field: 'description',
    export: false,
  },
  {
    field: 'file',
    export: false,
  },
];
