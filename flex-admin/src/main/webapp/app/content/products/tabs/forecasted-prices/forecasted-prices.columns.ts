import { Column } from '@app/shared/models';

export const COLUMNS: Column[] = [
  {
    field: 'actions',
    export: false,
  },
  {
    field: 'id',
  },
  {
    field: 'productFullName',
  },
  {
    field: 'forecastedPricesDate',
  },
  {
    field: 'createdBy',
  },
  {
    field: 'createdDate',
  },
  {
    field: 'delete',
    export: false,
  },
];
