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
    field: 'bsp',
    key: 'bsp.representative.companyName',
  },
  {
    field: 'schedulingUnitType',
    key: 'schedulingUnitType.nlsCode',
  },
  {
    field: 'couplingPoints',
    key: 'couplingPoints.name',
  },
  {
    field: 'primaryCouplingPoint',
    key: 'primaryCouplingPoint.name',
  },
  {
    field: 'units',
  },
  {
    field: 'numberOfDers',
  },
  {
    field: 'createdDate',
  },
  {
    field: 'lastModifiedDate',
  },
  {
    field: 'createdBy',
  },
  {
    field: 'lastModifiedBy',
  },
  {
    field: 'active',
  },
  {
    field: 'readyForTests',
  },
  {
    field: 'certified',
  },
  {
    field: 'delete',
    export: false,
  },
];
