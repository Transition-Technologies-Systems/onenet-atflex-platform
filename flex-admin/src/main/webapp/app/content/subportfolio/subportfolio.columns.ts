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
    field: 'name',
  },
  {
    field: 'numberOfDers',
  },
  {
    field: 'combinedPowerOfDers',
  },
  {
    field: 'couplingPointIdTypes',
    header: 'couplingPointId'
  },
  {
    field: 'mrid',
  },
  {
    field: 'fspa',
    key: 'fspa.representative.companyName',
  },
  {
    field: 'mrid',
  },
  {
    field: 'createdDate',
  },
  {
    field: 'lastModifiedDate',
  },
  {
    field: 'validFrom',
  },
  {
    field: 'validTo',
  },
  {
    field: 'active',
  },
  {
    field: 'certified',
  },
  {
    field: 'delete',
    export: false,
  },
];
