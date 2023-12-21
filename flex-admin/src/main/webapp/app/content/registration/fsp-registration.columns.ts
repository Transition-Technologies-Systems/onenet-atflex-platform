import { Column } from '@app/shared/models';

export const COLUMNS: Column[] = [
  {
    field: 'actions',
    style: { width: '40px' },
    export: false,
  },
  {
    field: 'id',
    header: 'applicationId',
  },
  {
    field: 'firstName',
    header: 'name',
  },
  {
    field: 'lastName',
  },
  {
    field: 'companyName',
  },
  {
    field: 'createdDate',
    header: 'creationDate',
  },
  {
    field: 'lastModifiedDate',
    header: 'lastUpdate',
  },
  {
    field: 'status',
    header: 'applicationStatus',
  },
];
