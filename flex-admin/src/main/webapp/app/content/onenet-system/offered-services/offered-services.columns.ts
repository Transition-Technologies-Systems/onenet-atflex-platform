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
    field: 'serviceCode',
  },
  {
    field: 'fileSchema',
    export: false,
  },
  {
    field: 'fileSchemaSample',
    export: false,
  },
  {
    field: 'description',
    export: false,
  },
  {
    field: 'provide',
    export: false,
  },
];
