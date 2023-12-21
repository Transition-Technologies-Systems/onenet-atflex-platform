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
    field: 'representativeCompanyName',
    header: 'companyName',
    key: 'representative.companyName',
  },
  {
    field: 'role',
  },
  {
    field: 'createdDate',
  },
  {
    field: 'lastModifiedDate',
  },
  {
    field: 'active',
  },
  {
    field: 'agreementWithTso',
  },
  {
    field: 'validFrom',
  },
  {
    field: 'validTo',
  },
  {
    field: 'representativeFirstName',
    header: 'representative.firstName',
    key: 'representative.firstName',
  },
  {
    field: 'representativeLastName',
    header: 'representative.lastName',
    key: 'representative.lastName',
  },
  {
    field: 'representativeEmail',
    header: 'representative.email',
    key: 'representative.email',
  },
  {
    field: 'representativePhoneNumber',
    header: 'representative.phoneNumber',
    key: 'representative.phoneNumber',
  },
  {
    field: 'delete',
    export: false,
  },
];
